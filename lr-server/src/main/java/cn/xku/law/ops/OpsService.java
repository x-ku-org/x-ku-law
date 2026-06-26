package cn.xku.law.ops;

import cn.xku.law.collect.domain.CollectRecordDO;
import cn.xku.law.collect.domain.CollectTaskDO;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawAiTaskDO;
import cn.xku.law.law.domain.LawProcessTaskDO;
import cn.xku.law.law.domain.DataAuditRecordDO;
import cn.xku.law.law.domain.DataQualityIssueDO;
import cn.xku.law.law.domain.SearchIndexTaskDO;
import cn.xku.law.law.domain.VectorSyncTaskDO;
import cn.xku.law.subscription.domain.AlertDeliveryDO;

import java.util.List;

/**
 * 平台运维只读监控 + 手动重试服务。面向平台管理员，统一查看采集/处理管线/索引/向量等
 * 调度任务表状态并对 failed 任务发起重试。所有任务表均为平台表（tenant_id=0），
 * 由 TenantLineHandlerImpl 白名单放行，不受租户过滤影响。
 */
public interface OpsService {

    PageResult<LawProcessTaskDO> pageProcessTasks(String status, long pageNo, long pageSize);

    /** 将 failed 处理任务重置为 pending（清错误、重置重试计数与时间戳），下轮调度重跑。 */
    boolean retryProcessTask(Long id);

    /** 批量将所有 failed 处理任务重置为 pending，返回重置条数。 */
    int retryAllProcessTasks();

    PageResult<CollectRecordDO> pageCollectRecords(String status, long pageNo, long pageSize);

    PageResult<CollectTaskDO> pageCollectTasks(long pageNo, long pageSize);

    PageResult<SearchIndexTaskDO> pageIndexTasks(String status, long pageNo, long pageSize);

    boolean retryIndexTask(Long id);

    /** 批量将所有 failed 索引同步任务重置为 pending，返回重置条数。 */
    int retryAllIndexTasks();

    /** 存量回填：为已发布且为当前版本、尚无在途 upsert 的版本批量入队 upsert 任务，返回入队条数。 */
    int backfillIndexTasks();

    PageResult<VectorSyncTaskDO> pageVectorTasks(String status, long pageNo, long pageSize);

    boolean retryVectorTask(Long id);

    /** 批量将所有 failed 向量同步任务重置为 pending，返回重置条数。 */
    int retryAllVectorTasks();

    PageResult<LawAiTaskDO> pageAiTasks(String status, long pageNo, long pageSize);

    boolean retryAiTask(Long id);

    /** 存量回填：为已发布且尚无 AI 任务/解读的版本批量入队 AI 任务，返回入队条数。 */
    int backfillAiTasks();

    PageResult<AlertDeliveryDO> pageAlertDeliveries(String status, long pageNo, long pageSize);

    /** 重投失败/待发的订阅预警投递记录（走真实站内信投递）。 */
    void retryAlertDelivery(Long id);

    /** 批量重投所有 failed 预警投递（走真实站内信投递），返回处理条数。 */
    int retryAllAlertDeliveries();

    PageResult<DataQualityIssueDO> pageQualityIssues(String status, String issueType, long pageNo, long pageSize);

    /** 标记质量问题已解决（置处理人/解决时间）。 */
    void resolveQualityIssue(Long id);

    PageResult<DataAuditRecordDO> pageAuditRecords(String auditType, long pageNo, long pageSize);

    /** 当前定时任务配置（只读，来自 application.yml / 环境变量）。 */
    List<OpsConfigVO> schedulerConfig();
}
