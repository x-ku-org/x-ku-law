package cn.xku.law.process;

import cn.xku.law.law.domain.LawAiTaskDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.mapper.LawAiTaskMapper;
import cn.xku.law.law.service.LawVersionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * AI 旁路队列消费者：消费 {@code lr_law_ai_task}，对版本跑 AI 阶段（元数据富集 → 解读）。
 * 结构化先行 / AI 旁路的「旁路」侧——与结构化主管线 {@link LawProcessTaskProcessor} 解耦：
 * 结构化回填（2 万+ 文件）完全不触发 LLM，AI 由本处理器单独消费，可单独开关与限流。
 *
 * <p>仅在 {@code app.process.ai.enabled=true} 时装配。消费模型（CAS 领取 / 重试 / 超时恢复 /
 * 受控并发）对齐 {@link LawProcessTaskProcessor}，但 batch/并发独立配置（{@code app.process.ai.*}），
 * 默认更小以尊重 LLM 限流。
 *
 * <p>每个任务：从 {@code lr_law_version.content_text} 回灌上下文正文（旁路无内存上下文）→
 * 顺序跑 AI 阶段 → 成功后重新入队检索索引（让新摘要/标签进入检索引擎）。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.process.ai", name = "enabled", havingValue = "true")
public class LawAiTaskProcessor {

    @Value("${app.process.ai.batch-size:5}")
    private int batchSize;

    @Value("${app.process.ai.max-retry:3}")
    private int maxRetry;

    @Value("${app.process.ai.processing-timeout-minutes:30}")
    private int processingTimeoutMinutes;

    private final LawAiTaskMapper taskMapper;
    private final LawVersionService lawVersionService;
    /** AI 阶段链（requiresAi）：元数据富集(25) → 解读(40)。 */
    private final List<LawProcessingStage> aiStages;
    private final ExecutorService executor;

    public LawAiTaskProcessor(LawAiTaskMapper taskMapper,
                              LawVersionService lawVersionService,
                              List<LawProcessingStage> stages,
                              @Value("${app.process.ai.concurrency:2}") int concurrency) {
        this.taskMapper = taskMapper;
        this.lawVersionService = lawVersionService;
        this.aiStages = stages.stream()
                .filter(LawProcessingStage::requiresAi)
                .sorted(Comparator.comparingInt(LawProcessingStage::order))
                .toList();
        int threads = Math.max(1, concurrency);
        this.executor = Executors.newFixedThreadPool(threads);
        log.info("[LawAi] AI pipeline stages: {} (concurrency={})",
                this.aiStages.stream().map(s -> s.order() + ":" + s.name()).toList(), threads);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    @Scheduled(fixedDelayString = "${app.process.ai.task-interval-ms:10000}")
    public void processPendingTasks() {
        List<LawAiTaskDO> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<LawAiTaskDO>()
                        .eq(LawAiTaskDO::getProcessStatus, "pending")
                        .lt(LawAiTaskDO::getRetryCount, maxRetry)
                        .last("LIMIT " + batchSize));
        if (tasks.isEmpty()) return;

        log.debug("[LawAi] processing {} pending AI tasks", tasks.size());
        List<Future<?>> futures = new ArrayList<>(tasks.size());
        for (LawAiTaskDO task : tasks) {
            futures.add(executor.submit(() -> processOneTask(task)));
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                log.error("[LawAi] batch task execution error: {}", e.getMessage(), e);
            }
        }
    }

    private void processOneTask(LawAiTaskDO task) {
        if (taskMapper.claimTask(task.getId()) == 0) {
            log.debug("[LawAi] task {} already claimed by another instance, skipping", task.getId());
            return;
        }
        LawVersionDO version = lawVersionService.getById(task.getVersionId());
        if (version == null) {
            log.warn("[LawAi] task {} versionId={} not found, mark done (nothing to do)",
                    task.getId(), task.getVersionId());
            markDone(task);
            return;
        }
        LawProcessingContext ctx = new LawProcessingContext(
                task.getId(), task.getDocumentId(), task.getVersionId(), task.getFileId());
        ctx.setExtractedText(version.getContentText());
        try {
            for (LawProcessingStage stage : aiStages) {
                stage.process(ctx);
            }
            // 富集可能写回 summary/标签，重新入队检索索引使其可被检索到。
            if (StringUtils.hasText(version.getContentText())) {
                lawVersionService.enqueueSearchIndex(task.getVersionId());
            }
            markDone(task);
        } catch (Exception e) {
            log.error("[LawAi] task {} (versionId={}) failed (attempt {}): {}",
                    task.getId(), task.getVersionId(), task.getRetryCount() + 1, e.getMessage(), e);
            markFailed(task, e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.process.ai.task-interval-ms:10000}")
    public void recoverStuckTasks() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
        List<LawAiTaskDO> stuckTasks = taskMapper.selectList(
                new LambdaQueryWrapper<LawAiTaskDO>()
                        .eq(LawAiTaskDO::getProcessStatus, "processing")
                        .lt(LawAiTaskDO::getUpdateTime, timeoutThreshold));
        if (stuckTasks.isEmpty()) return;

        log.warn("[LawAi] found {} stuck processing tasks (timeout={}min)", stuckTasks.size(), processingTimeoutMinutes);
        for (LawAiTaskDO task : stuckTasks) {
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            if (retries + 1 >= maxRetry) {
                taskMapper.update(null, new LambdaUpdateWrapper<LawAiTaskDO>()
                        .eq(LawAiTaskDO::getId, task.getId())
                        .eq(LawAiTaskDO::getProcessStatus, "processing")
                        .set(LawAiTaskDO::getProcessStatus, "failed")
                        .set(LawAiTaskDO::getRetryCount, retries + 1)
                        .set(LawAiTaskDO::getErrorMessage, "processing timeout, retries exhausted")
                        .set(LawAiTaskDO::getFinishedAt, LocalDateTime.now()));
                log.error("[LawAi] task {} timed out and exhausted retries, marked failed", task.getId());
            } else {
                taskMapper.update(null, new LambdaUpdateWrapper<LawAiTaskDO>()
                        .eq(LawAiTaskDO::getId, task.getId())
                        .eq(LawAiTaskDO::getProcessStatus, "processing")
                        .set(LawAiTaskDO::getProcessStatus, "pending")
                        .set(LawAiTaskDO::getRetryCount, retries + 1));
                log.warn("[LawAi] task {} timed out, reset to pending (attempt {})", task.getId(), retries + 1);
            }
        }
    }

    private void markDone(LawAiTaskDO task) {
        task.setProcessStatus("done");
        task.setErrorMessage(null);
        task.setFinishedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void markFailed(LawAiTaskDO task, String errorMsg) {
        int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
        task.setRetryCount(retries + 1);
        task.setErrorMessage(errorMsg != null && errorMsg.length() > 500
                ? errorMsg.substring(0, 500) : errorMsg);
        if (retries + 1 >= maxRetry) {
            task.setProcessStatus("failed");
            task.setFinishedAt(LocalDateTime.now());
            log.error("[LawAi] task {} exhausted retries, marked failed", task.getId());
        } else {
            task.setProcessStatus("pending");
        }
        taskMapper.updateById(task);
    }
}
