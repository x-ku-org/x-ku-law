package cn.xku.law.law.schedule;

import cn.xku.law.common.client.SearchClient;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.domain.SearchIndexTaskDO;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.mapper.LawVersionMapper;
import cn.xku.law.law.mapper.SearchIndexTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SearchIndexTaskProcessor {

    private static final String INDEX_NAME = "law_document";

    /** 每批领取数；大批量回填时可调大以加速。 */
    @Value("${app.search.batch-size:50}")
    private int batchSize;

    /** 受控并发数；ES 写入为远程 IO，提高并发显著提升吞吐。 */
    @Value("${app.search.concurrency:8}")
    private int concurrency;

    @Value("${app.search.max-retry:3}")
    private int maxRetry;

    @Value("${app.search.processing-timeout-minutes:5}")
    private int processingTimeoutMinutes;

    private final SearchIndexTaskMapper taskMapper;
    private final LawVersionMapper lawVersionMapper;
    private final LawDocumentMapper lawDocumentMapper;
    private final SearchClient searchClient;

    /** 受控并发执行器：每批 pending 任务并行处理，CAS 领取保证不重复。 */
    private ExecutorService executor;

    @PostConstruct
    public void init() {
        this.executor = Executors.newFixedThreadPool(Math.max(1, concurrency));
        log.info("[IndexTask] processor started (batchSize={}, concurrency={})", batchSize, Math.max(1, concurrency));
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) executor.shutdown();
    }

    @Scheduled(fixedDelayString = "${app.search.index-task-interval-ms:10000}")
    public void processPendingTasks() {
        List<SearchIndexTaskDO> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<SearchIndexTaskDO>()
                        .eq(SearchIndexTaskDO::getSyncStatus, "pending")
                        .lt(SearchIndexTaskDO::getRetryCount, maxRetry)
                        .last("LIMIT " + batchSize));

        if (tasks.isEmpty()) return;

        log.debug("[IndexTask] processing {} pending tasks", tasks.size());
        // 受控并发：提交到固定线程池并等待本批完成；CAS 领取保证同一任务不被重复处理。
        List<Future<?>> futures = new ArrayList<>(tasks.size());
        for (SearchIndexTaskDO task : tasks) {
            futures.add(executor.submit(() -> processOneTask(task)));
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                log.error("[IndexTask] batch task execution error: {}", e.getMessage(), e);
            }
        }
    }

    private void processOneTask(SearchIndexTaskDO task) {
        if (taskMapper.claimTask(task.getId()) == 0) {
            log.debug("[IndexTask] task {} already claimed by another instance, skipping", task.getId());
            return;
        }
        try {
            if ("upsert".equals(task.getActionType())) {
                doUpsert(task);
            } else if ("delete".equals(task.getActionType())) {
                doDelete(task);
            } else {
                log.warn("[IndexTask] unknown actionType '{}', taskId={}", task.getActionType(), task.getId());
            }
            markDone(task);
        } catch (Exception e) {
            log.error("[IndexTask] task {} failed (attempt {}): {}", task.getId(), task.getRetryCount() + 1, e.getMessage());
            markFailed(task, e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.search.index-task-interval-ms:10000}")
    public void recoverStuckTasks() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
        List<SearchIndexTaskDO> stuckTasks = taskMapper.selectList(
                new LambdaQueryWrapper<SearchIndexTaskDO>()
                        .eq(SearchIndexTaskDO::getSyncStatus, "processing")
                        .lt(SearchIndexTaskDO::getUpdateTime, timeoutThreshold));

        if (stuckTasks.isEmpty()) return;

        log.warn("[IndexTask] found {} stuck processing tasks (timeout={}min)", stuckTasks.size(), processingTimeoutMinutes);
        for (SearchIndexTaskDO task : stuckTasks) {
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            if (retries + 1 >= maxRetry) {
                taskMapper.update(null, new LambdaUpdateWrapper<SearchIndexTaskDO>()
                        .eq(SearchIndexTaskDO::getId, task.getId())
                        .eq(SearchIndexTaskDO::getSyncStatus, "processing")
                        .set(SearchIndexTaskDO::getSyncStatus, "failed")
                        .set(SearchIndexTaskDO::getRetryCount, retries + 1)
                        .set(SearchIndexTaskDO::getErrorMessage, "processing timeout, retries exhausted"));
                log.error("[IndexTask] task {} timed out and exhausted retries, marked failed", task.getId());
            } else {
                taskMapper.update(null, new LambdaUpdateWrapper<SearchIndexTaskDO>()
                        .eq(SearchIndexTaskDO::getId, task.getId())
                        .eq(SearchIndexTaskDO::getSyncStatus, "processing")
                        .set(SearchIndexTaskDO::getSyncStatus, "pending")
                        .set(SearchIndexTaskDO::getRetryCount, retries + 1));
                log.warn("[IndexTask] task {} timed out, reset to pending (attempt {})", task.getId(), retries + 1);
            }
        }
    }

    private void doUpsert(SearchIndexTaskDO task) {
        if (!"law_version".equals(task.getRefType())) {
            log.warn("[IndexTask] unsupported refType '{}', taskId={}", task.getRefType(), task.getId());
            return;
        }
        LawVersionDO version = lawVersionMapper.selectById(task.getRefId());
        if (version == null) {
            log.warn("[IndexTask] LawVersion not found: refId={}", task.getRefId());
            return;
        }
        LawDocumentDO doc = lawDocumentMapper.selectById(version.getDocumentId());

        Map<String, Object> source = new HashMap<>();
        source.put("versionId",        version.getId());
        source.put("documentId",        version.getDocumentId());
        source.put("title",             doc != null ? doc.getTitle() : "");
        source.put("docNumber",         doc != null ? doc.getDocumentNo() : "");
        source.put("contentText",       version.getContentText() != null ? version.getContentText() : "");
        source.put("effectLevel",       doc != null ? doc.getLegalLevel() : "");
        source.put("status",            doc != null ? doc.getStatus() : "");
        source.put("publishAuthority",  doc != null ? doc.getIssuingOrg() : "");
        source.put("regionCode",         doc != null ? doc.getRegionCode() : null);
        source.put("effectiveDate",     version.getEffectiveDate() != null ? version.getEffectiveDate().toString() : null);
        source.put("tenantId",          0L);
        source.put("isPublic",          true);

        searchClient.indexDocument(INDEX_NAME, String.valueOf(task.getRefId()), source);
    }

    private void doDelete(SearchIndexTaskDO task) {
        searchClient.deleteDocument(INDEX_NAME, String.valueOf(task.getRefId()));
    }

    private void markDone(SearchIndexTaskDO task) {
        task.setSyncStatus("done");
        task.setLastSyncTime(LocalDateTime.now());
        task.setErrorMessage(null);
        taskMapper.updateById(task);
    }

    private void markFailed(SearchIndexTaskDO task, String errorMsg) {
        int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
        task.setRetryCount(retries + 1);
        task.setErrorMessage(errorMsg != null && errorMsg.length() > 500
                ? errorMsg.substring(0, 500) : errorMsg);
        task.setLastSyncTime(LocalDateTime.now());
        if (retries + 1 >= maxRetry) {
            task.setSyncStatus("failed");
            log.error("[IndexTask] task {} exhausted retries, marked failed", task.getId());
        } else {
            task.setSyncStatus("pending");
        }
        taskMapper.updateById(task);
    }
}
