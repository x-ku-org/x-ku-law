package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 法规 AI 处理任务，对应 lr_law_ai_task。
 * 结构化先行 / AI 旁路：当 app.process.ai.enabled=true 时，AiTaskEnqueueStage 为发布版本入队一条任务，
 * 由 lr-server 的 LawAiTaskProcessor 异步消费，跑 AI 阶段（元数据富集 → 解读）。
 * 结构与消费模型对齐 {@link LawProcessTaskDO}（CAS 领取 / 重试 / 超时恢复）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_ai_task")
public class LawAiTaskDO extends BaseDO {

    private Long documentId;
    private Long versionId;
    /** 正文文件 ID，可为空 */
    private Long fileId;
    /** pending/processing/done/failed */
    private String processStatus;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
