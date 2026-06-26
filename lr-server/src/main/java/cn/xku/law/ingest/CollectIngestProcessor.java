package cn.xku.law.ingest;

import cn.xku.law.collect.domain.CollectRecordDO;
import cn.xku.law.collect.service.CollectIngestService;
import cn.xku.law.collect.source.SourceAdapter;
import cn.xku.law.collect.source.SourceAdapterRegistry;
import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 采集接入编排器：定时扫描对象存储中的运行文件夹（fglaw* / gblaw*），把元数据接入暂存层并提升为法规。
 * 仅在 {@code app.collect.enabled=true} 时启用。轮询/认领/超时恢复风格对齐 SearchIndexTaskProcessor。
 *
 * <p>放在 lr-server（组合根）：唯一能同时访问 FileService、FileStorageClient 与法规域服务的地方。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.collect", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class CollectIngestProcessor {

    @Value("${app.collect.batch-size:5}")
    private int batchSize;

    @Value("${app.collect.processing-timeout-minutes:30}")
    private int processingTimeoutMinutes;

    private final FileStorageClient fileStorageClient;
    private final CollectIngestService ingestService;
    private final CollectItemPromoter promoter;
    private final SourceAdapterRegistry sourceAdapterRegistry;
    private final ObjectMapper objectMapper;

    /**
     * 定时批量接入：按 cron 在约定时刻跑一次（默认每月 1 日 03:00，对齐 V5 种子的 cron_expr），
     * 而非每隔数分钟轮询扫描对象存储。仍以「运行文件夹内 metadata.json 存在」作为采集完成信号。
     * 手动即时接入走 {@link CollectIngestController#triggerScan}。
     */
    @Scheduled(cron = "${app.collect.cron:0 0 3 1 * ?}")
    public void scheduledScanAndIngest() {
        scanAndIngest(null, null);
    }

    /**
     * 扫描对象存储并接入采集批次。{@code sourceCode} 为空时扫描全部已注册源；
     * {@code batchSizeOverride} 可临时放大单次处理的文件夹数（手动触发时用）。
     */
    public CollectIngestScanResult scanAndIngest(String sourceCode, Integer batchSizeOverride) {
        List<SourceAdapter> adapters = resolveAdapters(sourceCode);
        int effectiveBatchSize = batchSizeOverride != null && batchSizeOverride > 0
                ? batchSizeOverride : batchSize;

        List<CollectIngestScanResult.SourceScanResult> sourceResults = new ArrayList<>();
        int totalProcessed = 0;
        for (SourceAdapter adapter : adapters) {
            try {
                int processed = scanPrefix(adapter.folderPrefix(), adapter.sourceCode(),
                        adapter.sourceName(), adapter.metadataFileName(), effectiveBatchSize);
                totalProcessed += processed;
                sourceResults.add(new CollectIngestScanResult.SourceScanResult(
                        adapter.sourceCode(), processed, null));
            } catch (Exception e) {
                log.error("[CollectIngest] scan failed for source '{}': {}",
                        adapter.sourceCode(), e.getMessage(), e);
                sourceResults.add(new CollectIngestScanResult.SourceScanResult(
                        adapter.sourceCode(), 0, e.getMessage()));
            }
        }
        return new CollectIngestScanResult(sourceResults, totalProcessed);
    }

    /** 将卡在 processing 且超时的标记重置为 pending，返回重置条数。 */
    public int recoverStuckRecordsNow() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
        int reset = ingestService.recoverStuckRecords(threshold);
        if (reset > 0) {
            log.warn("[CollectIngest] reset {} stuck processing records to pending (timeout={}min)",
                    reset, processingTimeoutMinutes);
        }
        return reset;
    }

    private List<SourceAdapter> resolveAdapters(String sourceCode) {
        if (!StringUtils.hasText(sourceCode)) {
            return sourceAdapterRegistry.all();
        }
        SourceAdapter adapter = sourceAdapterRegistry.resolve(sourceCode.trim());
        if (adapter == null) {
            throw new AppException(ErrorCode.PARAM_ERROR, "未知的数据源编码: " + sourceCode);
        }
        return List.of(adapter);
    }

    private int scanPrefix(String prefix, String parserCode, String sourceName, String metadataFile,
                           int effectiveBatchSize) {
        List<String> keys = fileStorageClient.list(prefix);
        if (keys.isEmpty()) return 0;

        // 运行文件夹 = 对象 key 的首段（含末尾 '/'），如 "fglaw20260501/"
        Set<String> folders = keys.stream()
                .map(CollectIngestProcessor::topFolder)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> keySet = new HashSet<>(keys);

        Long sourceId = ingestService.resolveSourceIdByName(sourceName);
        Long taskId = ingestService.resolveTaskIdByParserCode(parserCode);
        if (sourceId == null || taskId == null) {
            log.warn("[CollectIngest] source/task not seeded for parser '{}' (sourceId={}, taskId={}), skip",
                    parserCode, sourceId, taskId);
            return 0;
        }

        int processed = 0;
        for (String folder : folders) {
            if (processed >= effectiveBatchSize) break;
            String metaKey = folder + metadataFile;
            if (!keySet.contains(metaKey)) continue; // 运行未完成：元数据最后上传，缺失即跳过

            String marker = "minio://" + folder;
            // folder（如 "gblaw20260501/"）作为幂等键存入 content_hash，由 uk_collect_record_marker 唯一约束保证
            CollectRecordDO record = ingestService.findOrCreateMarker(taskId, sourceId, folder, marker);
            if (!"pending".equals(record.getCollectStatus())) {
                continue; // processing/success/failed 均不重复处理
            }
            Long recordId = record.getId();
            if (!ingestService.claimRecord(recordId)) continue; // 被其它实例抢先 CAS

            processed++;
            try {
                processFolder(parserCode, folder, metaKey, keySet, sourceId, recordId);
                ingestService.closeCollectRecord(recordId, "success", null);
            } catch (Exception e) {
                log.error("[CollectIngest] folder {} failed: {}", folder, e.getMessage(), e);
                ingestService.closeCollectRecord(recordId, "failed", e.getMessage());
            }
        }
        return processed;
    }

    private void processFolder(String parserCode, String folder, String metaKey, Set<String> keySet,
                               Long sourceId, Long recordId) throws Exception {
        byte[] bytes = fileStorageClient.download(metaKey);
        List<Map<String, Object>> items =
                objectMapper.readValue(bytes, new TypeReference<List<Map<String, Object>>>() {});

        String batchNo = folder.replace("/", "") + "-" + UUID.randomUUID().toString().substring(0, 8);
        Long batchId = ingestService.openImportBatch(batchNo, sourceId);

        int ok = 0, fail = 0;
        for (int i = 0; i < items.size(); i++) {
            int rowNo = i + 1;
            try {
                promoter.promote(items.get(i), parserCode, folder, keySet, sourceId, recordId, batchId, rowNo);
                ok++;
            } catch (Exception e) {
                log.warn("[CollectIngest] item rowNo={} in {} failed: {}", rowNo, folder, e.getMessage());
                ingestService.writeFailedImportRecord(batchId, sourceId, rowNo, e.getMessage());
                fail++;
            }
        }
        ingestService.closeImportBatch(batchId, items.size(), ok, fail, fail == 0 ? "success" : "partial");
        log.info("[CollectIngest] folder {} done: total={}, ok={}, fail={}", folder, items.size(), ok, fail);
    }

    @Scheduled(fixedDelayString = "${app.collect.scan-interval-ms:300000}")
    public void scheduledRecoverStuckRecords() {
        recoverStuckRecordsNow();
    }

    /** 取对象 key 的首段文件夹（含末尾 '/'）；无子路径返回 null */
    private static String topFolder(String key) {
        int i = key.indexOf('/');
        return i > 0 ? key.substring(0, i + 1) : null;
    }
}
