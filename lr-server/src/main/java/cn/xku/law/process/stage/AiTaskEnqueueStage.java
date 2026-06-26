package cn.xku.law.process.stage;

import cn.xku.law.law.domain.LawAiTaskDO;
import cn.xku.law.law.mapper.LawAiTaskMapper;
import cn.xku.law.process.LawProcessingContext;
import cn.xku.law.process.LawProcessingStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 阶段 60：AI 旁路入队（结构化阶段，不触发 LLM）。在发布(30)、变更分析(50)之后，
 * 当 {@code app.process.ai.enabled=true} 时，为当前版本入队一条 {@code lr_law_ai_task}，
 * 交由 {@code LawAiTaskProcessor} 旁路消费（元数据富集 → 解读）。
 *
 * <p>结构化先行：开关关闭（默认，含 2 万+ 历史回填）时本阶段空跑，整条结构化管线不碰 AI。
 * 开关打开（增量上传/采集）时即时入队。存量历史版本另由 {@code POST /ops/ai-tasks/backfill} 批量回填。
 * 入队前用 {@link LawAiTaskMapper#countActiveByVersion} 去重，唯一键兜底防并发重复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiTaskEnqueueStage implements LawProcessingStage {

    @Value("${app.process.ai.enabled:false}")
    private boolean aiEnabled;

    private final LawAiTaskMapper aiTaskMapper;

    @Override
    public String name() {
        return "ai-task-enqueue";
    }

    @Override
    public int order() {
        return 60;
    }

    @Override
    public void process(LawProcessingContext ctx) {
        if (!aiEnabled) {
            return;
        }
        if (ctx.getVersionId() == null) {
            return;
        }
        if (aiTaskMapper.countActiveByVersion(ctx.getVersionId()) > 0) {
            log.debug("[AiEnqueue] versionId={} already has in-flight AI task, skip", ctx.getVersionId());
            return;
        }
        LawAiTaskDO task = new LawAiTaskDO();
        task.setDocumentId(ctx.getDocumentId());
        task.setVersionId(ctx.getVersionId());
        task.setFileId(ctx.getFileId());
        task.setProcessStatus("pending");
        task.setRetryCount(0);
        try {
            aiTaskMapper.insert(task);
            log.info("[AiEnqueue] versionId={} enqueued AI task", ctx.getVersionId());
        } catch (DuplicateKeyException e) {
            log.debug("[AiEnqueue] versionId={} AI task already enqueued (concurrent), skip", ctx.getVersionId());
        }
    }
}
