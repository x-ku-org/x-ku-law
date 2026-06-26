package cn.xku.law.ops;

import cn.xku.law.collect.domain.CollectRecordDO;
import cn.xku.law.collect.domain.CollectTaskDO;
import cn.xku.law.collect.mapper.CollectRecordMapper;
import cn.xku.law.collect.mapper.CollectTaskMapper;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawAiTaskDO;
import cn.xku.law.law.domain.LawProcessTaskDO;
import cn.xku.law.law.domain.SearchIndexTaskDO;
import cn.xku.law.law.domain.VectorSyncTaskDO;
import cn.xku.law.law.mapper.LawAiTaskMapper;
import cn.xku.law.law.mapper.LawProcessTaskMapper;
import cn.xku.law.law.domain.DataAuditRecordDO;
import cn.xku.law.law.domain.DataQualityIssueDO;
import cn.xku.law.law.mapper.DataAuditRecordMapper;
import cn.xku.law.law.mapper.DataQualityIssueMapper;
import cn.xku.law.law.mapper.SearchIndexTaskMapper;
import cn.xku.law.law.mapper.VectorSyncTaskMapper;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.subscription.domain.AlertDeliveryDO;
import cn.xku.law.subscription.service.AlertDeliveryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpsServiceImpl implements OpsService {

    private final LawProcessTaskMapper processTaskMapper;
    private final LawAiTaskMapper aiTaskMapper;
    private final CollectRecordMapper collectRecordMapper;
    private final CollectTaskMapper collectTaskMapper;
    private final SearchIndexTaskMapper indexTaskMapper;
    private final VectorSyncTaskMapper vectorTaskMapper;
    private final AlertDeliveryService alertDeliveryService;
    private final DataQualityIssueMapper qualityIssueMapper;
    private final DataAuditRecordMapper auditRecordMapper;
    private final Environment env;

    @Override
    public PageResult<LawProcessTaskDO> pageProcessTasks(String status, long pageNo, long pageSize) {
        LambdaQueryWrapper<LawProcessTaskDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(LawProcessTaskDO::getProcessStatus, status);
        }
        w.orderByDesc(LawProcessTaskDO::getId);
        return PageResult.of(processTaskMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public boolean retryProcessTask(Long id) {
        LambdaUpdateWrapper<LawProcessTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(LawProcessTaskDO::getId, id)
                .eq(LawProcessTaskDO::getProcessStatus, "failed")
                .set(LawProcessTaskDO::getProcessStatus, "pending")
                .set(LawProcessTaskDO::getRetryCount, 0)
                .set(LawProcessTaskDO::getErrorMessage, null)
                .set(LawProcessTaskDO::getStartedAt, null)
                .set(LawProcessTaskDO::getFinishedAt, null);
        return processTaskMapper.update(null, u) > 0;
    }

    @Override
    public int retryAllProcessTasks() {
        LambdaUpdateWrapper<LawProcessTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(LawProcessTaskDO::getProcessStatus, "failed")
                .set(LawProcessTaskDO::getProcessStatus, "pending")
                .set(LawProcessTaskDO::getRetryCount, 0)
                .set(LawProcessTaskDO::getErrorMessage, null)
                .set(LawProcessTaskDO::getStartedAt, null)
                .set(LawProcessTaskDO::getFinishedAt, null);
        return processTaskMapper.update(null, u);
    }

    @Override
    public PageResult<CollectRecordDO> pageCollectRecords(String status, long pageNo, long pageSize) {
        LambdaQueryWrapper<CollectRecordDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(CollectRecordDO::getCollectStatus, status);
        }
        w.orderByDesc(CollectRecordDO::getId);
        return PageResult.of(collectRecordMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public PageResult<CollectTaskDO> pageCollectTasks(long pageNo, long pageSize) {
        LambdaQueryWrapper<CollectTaskDO> w = new LambdaQueryWrapper<>();
        w.orderByDesc(CollectTaskDO::getId);
        return PageResult.of(collectTaskMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public PageResult<SearchIndexTaskDO> pageIndexTasks(String status, long pageNo, long pageSize) {
        LambdaQueryWrapper<SearchIndexTaskDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(SearchIndexTaskDO::getSyncStatus, status);
        }
        w.orderByDesc(SearchIndexTaskDO::getId);
        return PageResult.of(indexTaskMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public boolean retryIndexTask(Long id) {
        LambdaUpdateWrapper<SearchIndexTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(SearchIndexTaskDO::getId, id)
                .eq(SearchIndexTaskDO::getSyncStatus, "failed")
                .set(SearchIndexTaskDO::getSyncStatus, "pending")
                .set(SearchIndexTaskDO::getRetryCount, 0)
                .set(SearchIndexTaskDO::getErrorMessage, null);
        return indexTaskMapper.update(null, u) > 0;
    }

    @Override
    public int retryAllIndexTasks() {
        LambdaUpdateWrapper<SearchIndexTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(SearchIndexTaskDO::getSyncStatus, "failed")
                .set(SearchIndexTaskDO::getSyncStatus, "pending")
                .set(SearchIndexTaskDO::getRetryCount, 0)
                .set(SearchIndexTaskDO::getErrorMessage, null);
        return indexTaskMapper.update(null, u);
    }

    @Override
    public int backfillIndexTasks() {
        return indexTaskMapper.backfillPublishedCurrent();
    }

    @Override
    public PageResult<VectorSyncTaskDO> pageVectorTasks(String status, long pageNo, long pageSize) {
        LambdaQueryWrapper<VectorSyncTaskDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(VectorSyncTaskDO::getSyncStatus, status);
        }
        w.orderByDesc(VectorSyncTaskDO::getId);
        return PageResult.of(vectorTaskMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public boolean retryVectorTask(Long id) {
        LambdaUpdateWrapper<VectorSyncTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(VectorSyncTaskDO::getId, id)
                .eq(VectorSyncTaskDO::getSyncStatus, "failed")
                .set(VectorSyncTaskDO::getSyncStatus, "pending")
                .set(VectorSyncTaskDO::getRetryCount, 0)
                .set(VectorSyncTaskDO::getErrorMessage, null);
        return vectorTaskMapper.update(null, u) > 0;
    }

    @Override
    public int retryAllVectorTasks() {
        LambdaUpdateWrapper<VectorSyncTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(VectorSyncTaskDO::getSyncStatus, "failed")
                .set(VectorSyncTaskDO::getSyncStatus, "pending")
                .set(VectorSyncTaskDO::getRetryCount, 0)
                .set(VectorSyncTaskDO::getErrorMessage, null);
        return vectorTaskMapper.update(null, u);
    }

    @Override
    public PageResult<LawAiTaskDO> pageAiTasks(String status, long pageNo, long pageSize) {
        LambdaQueryWrapper<LawAiTaskDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(LawAiTaskDO::getProcessStatus, status);
        }
        w.orderByDesc(LawAiTaskDO::getId);
        return PageResult.of(aiTaskMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public boolean retryAiTask(Long id) {
        LambdaUpdateWrapper<LawAiTaskDO> u = new LambdaUpdateWrapper<>();
        u.eq(LawAiTaskDO::getId, id)
                .eq(LawAiTaskDO::getProcessStatus, "failed")
                .set(LawAiTaskDO::getProcessStatus, "pending")
                .set(LawAiTaskDO::getRetryCount, 0)
                .set(LawAiTaskDO::getErrorMessage, null)
                .set(LawAiTaskDO::getStartedAt, null)
                .set(LawAiTaskDO::getFinishedAt, null);
        return aiTaskMapper.update(null, u) > 0;
    }

    @Override
    public int backfillAiTasks() {
        return aiTaskMapper.backfillPublishedMissing();
    }

    @Override
    public PageResult<AlertDeliveryDO> pageAlertDeliveries(String status, long pageNo, long pageSize) {
        LambdaQueryWrapper<AlertDeliveryDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(AlertDeliveryDO::getSendStatus, status);
        }
        w.orderByDesc(AlertDeliveryDO::getId);
        return PageResult.of(alertDeliveryService.getBaseMapper().selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public void retryAlertDelivery(Long id) {
        alertDeliveryService.retry(id);
    }

    @Override
    public int retryAllAlertDeliveries() {
        LambdaQueryWrapper<AlertDeliveryDO> w = new LambdaQueryWrapper<>();
        w.eq(AlertDeliveryDO::getSendStatus, "failed").select(AlertDeliveryDO::getId);
        List<AlertDeliveryDO> failed = alertDeliveryService.getBaseMapper().selectList(w);
        int n = 0;
        for (AlertDeliveryDO d : failed) {
            try {
                alertDeliveryService.retry(d.getId());
                n++;
            } catch (Exception ignored) {
                // 单条重投异常不影响其余记录，继续重投
            }
        }
        return n;
    }

    @Override
    public PageResult<DataQualityIssueDO> pageQualityIssues(String status, String issueType,
                                                            long pageNo, long pageSize) {
        LambdaQueryWrapper<DataQualityIssueDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            w.eq(DataQualityIssueDO::getStatus, status);
        }
        if (StringUtils.hasText(issueType)) {
            w.eq(DataQualityIssueDO::getIssueType, issueType);
        }
        w.orderByDesc(DataQualityIssueDO::getId);
        return PageResult.of(qualityIssueMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public void resolveQualityIssue(Long id) {
        DataQualityIssueDO issue = qualityIssueMapper.selectById(id);
        if (issue == null) throw new AppException(ErrorCode.NOT_FOUND);
        issue.setStatus("resolved");
        issue.setOwnerUserId(SecurityUtils.getCurrentUserId());
        issue.setResolvedTime(java.time.LocalDateTime.now());
        qualityIssueMapper.updateById(issue);
    }

    @Override
    public PageResult<DataAuditRecordDO> pageAuditRecords(String auditType, long pageNo, long pageSize) {
        LambdaQueryWrapper<DataAuditRecordDO> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(auditType)) {
            w.eq(DataAuditRecordDO::getAuditType, auditType);
        }
        w.orderByDesc(DataAuditRecordDO::getId);
        return PageResult.of(auditRecordMapper.selectPage(Page.of(pageNo, pageSize), w));
    }

    @Override
    public List<OpsConfigVO> schedulerConfig() {
        List<OpsConfigVO> list = new ArrayList<>();

        list.add(new OpsConfigVO(
                "collect", "采集接入",
                env.getProperty("app.collect.enabled", Boolean.class, false),
                "cron",
                env.getProperty("app.collect.cron", "0 0 3 1 * ?"),
                null,
                "app.collect.* (COLLECT_ENABLED / COLLECT_CRON / COLLECT_BATCH_SIZE)",
                "定时扫描 MinIO 采集产物并接入；单批 "
                        + env.getProperty("app.collect.batch-size", "5") + " 个运行文件夹；"
                        + "卡住记录重置轮询 " + env.getProperty("app.collect.scan-interval-ms", "300000") + "ms"));

        list.add(new OpsConfigVO(
                "process", "处理管线",
                env.getProperty("app.process.enabled", Boolean.class, false),
                "interval",
                interval(env.getProperty("app.process.task-interval-ms", "10000")),
                env.getProperty("app.process.max-retry", Integer.class, 3),
                "app.process.* (PROCESS_ENABLED / PROCESS_INTERVAL / PROCESS_MAX_RETRY)",
                "结构化主管线：文本提取→分段→发布→变更分析（不触发 LLM）；单批 "
                        + env.getProperty("app.process.batch-size", "20") + "，并发 "
                        + env.getProperty("app.process.concurrency", "4")));

        list.add(new OpsConfigVO(
                "process-ai", "AI 旁路（摘要/解读）",
                env.getProperty("app.process.ai.enabled", Boolean.class, false),
                "interval",
                interval(env.getProperty("app.process.ai.task-interval-ms", "10000")),
                env.getProperty("app.process.ai.max-retry", Integer.class, 3),
                "app.process.ai.* (PROCESS_AI_ENABLED) + app.ai.*（需 AI 模型 apikey）",
                "元数据富集（摘要/标签）→ 整篇解读；单批 "
                        + env.getProperty("app.process.ai.batch-size", "5") + "，并发 "
                        + env.getProperty("app.process.ai.concurrency", "2")
                        + "；存量回填 POST /ops/ai-tasks/backfill"));

        list.add(new OpsConfigVO(
                "search", "检索索引同步",
                env.getProperty("app.search.enabled", Boolean.class, true),
                "interval",
                interval(env.getProperty("app.search.index-task-interval-ms", "10000")),
                env.getProperty("app.search.max-retry", Integer.class, 3),
                "app.search.* (SEARCH_ENABLED)",
                "同步法规文档到检索引擎；存量回填 POST /ops/index-tasks/backfill"));

        list.add(new OpsConfigVO(
                "vector", "向量同步",
                env.getProperty("app.vector.enabled", Boolean.class, false),
                "interval",
                interval(env.getProperty("app.vector.sync-task-interval-ms", "10000")),
                env.getProperty("app.vector.max-retry", Integer.class, 3),
                "app.vector.* (VECTOR_ENABLED / EMBED_ENABLED)",
                "条款分片向量化并写入向量索引；处理超时 "
                        + env.getProperty("app.vector.processing-timeout-minutes", "5") + " 分钟"));

        return list;
    }

    private static String interval(String ms) {
        try {
            long v = Long.parseLong(ms);
            return v % 1000 == 0 ? ("每 " + (v / 1000) + " 秒") : ("每 " + v + " 毫秒");
        } catch (NumberFormatException e) {
            return "每 " + ms + " 毫秒";
        }
    }
}
