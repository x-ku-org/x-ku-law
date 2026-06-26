package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 法规处理管线任务，对应 lr_law_process_task。
 * 两条入口（采集批量 / 管理员上传）建好草稿版本后入队一条 pending 任务，
 * 由 lr-server 的 LawProcessTaskProcessor 异步消费，顺序跑「文本提取→分段→发布→解读→变更分析」各阶段。
 * 结构与消费模型对齐 {@link SearchIndexTaskDO} / {@link VectorSyncTaskDO}（CAS 领取 / 重试 / 超时恢复）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_process_task")
public class LawProcessTaskDO extends BaseDO {

    private Long documentId;
    private Long versionId;
    /** 正文文件 ID，可为空（仅元数据接入时无文件，提取/分段阶段会跳过） */
    private Long fileId;
    /** pending/processing/done/failed */
    private String processStatus;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
