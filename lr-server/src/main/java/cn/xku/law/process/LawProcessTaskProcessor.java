package cn.xku.law.process;

import cn.xku.law.law.domain.LawProcessTaskDO;
import cn.xku.law.law.mapper.LawProcessTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

/**
 * 法规处理管线消费者：读取 lr_law_process_task 的 pending 任务，按阶段链顺序处理某法规版本
 * （文本提取 → 分段 → 发布 → 解读 → 变更分析）。两条接入入口（采集批量 / 管理员上传）
 * 都只负责建版本 + 入队，处理逻辑全部收敛到此处的统一管线，保证两路产出一致。
 *
 * <p>触发方式有两条：
 * <ul>
 *   <li><b>管理员上传</b>：上传接入事务提交后由上传链路调用 {@link #triggerNow(Long)} 异步<b>立刻</b>处理，
 *       不必等待轮询，用户上传即处理。</li>
 *   <li><b>采集批量（MinIO）</b>：由 {@code CollectIngestProcessor} 按 cron <b>定时拉取</b>并入队，
 *       入队任务由下面的 {@link #processPendingTasks()} 轮询消费。</li>
 * </ul>
 * 因此 {@link #processPendingTasks()} 轮询主要承担两个职责：消费定时拉取入队的任务、以及兜底重试
 * （失败/超时被重置为 pending 的任务，含 triggerNow 因实例重启等原因漏处理的）。
 *
 * <p>放在 lr-server（组合根）：唯一能同时访问 FileService / FileStorageClient / 法规域服务 /
 * EmbeddingClient / AiChatModelRegistry 的地方。消费模型（CAS 领取 / 重试 / 超时恢复）对齐
 * {@code SearchIndexTaskProcessor} / {@code VectorSyncTaskProcessor}。仅在 app.process.enabled=true 时启用。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.process", name = "enabled", havingValue = "true")
public class LawProcessTaskProcessor {

    @Value("${app.process.batch-size:20}")
    private int batchSize;

    @Value("${app.process.max-retry:3}")
    private int maxRetry;

    @Value("${app.process.processing-timeout-minutes:30}")
    private int processingTimeoutMinutes;

    /** 单个任务内遇数据库锁竞争（死锁/锁等待超时）的就地重试上限，超过则上抛走失败重试策略。 */
    private static final int LOCK_RETRY_MAX_ATTEMPTS = 3;

    private final LawProcessTaskMapper taskMapper;
    /** 结构化阶段链（!requiresAi）：提取/分段/发布/变更分析/AI入队。AI 阶段（富集/解读）由 LawAiTaskProcessor 旁路消费。 */
    private final List<LawProcessingStage> structuralStages;
    /** 受控并发执行器：每批 pending 任务并行处理，CAS 领取保证不重复。 */
    private final ExecutorService executor;

    public LawProcessTaskProcessor(LawProcessTaskMapper taskMapper,
                                   List<LawProcessingStage> stages,
                                   @Value("${app.process.concurrency:4}") int concurrency) {
        this.taskMapper = taskMapper;
        this.structuralStages = stages.stream()
                .filter(s -> !s.requiresAi())
                .sorted(Comparator.comparingInt(LawProcessingStage::order))
                .toList();
        int threads = Math.max(1, concurrency);
        this.executor = Executors.newFixedThreadPool(threads);
        log.info("[LawProcess] structural pipeline stages: {} (concurrency={})",
                this.structuralStages.stream().map(s -> s.order() + ":" + s.name()).toList(), threads);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 立刻触发某版本的处理：供管理员上传链路在接入事务<b>提交后</b>异步调用，避免等待轮询。
     * 异步执行（{@link Async}），上传接口不被处理耗时阻塞；CAS 领取与轮询互斥，重复触发安全。
     * 找不到在途 pending 任务（如已被轮询抢先或为重复版本未入队）则静默跳过。
     */
    @Async
    public void triggerNow(Long versionId) {
        if (versionId == null) return;
        List<LawProcessTaskDO> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<LawProcessTaskDO>()
                        .eq(LawProcessTaskDO::getVersionId, versionId)
                        .eq(LawProcessTaskDO::getProcessStatus, "pending")
                        .lt(LawProcessTaskDO::getRetryCount, maxRetry)
                        .last("LIMIT 1"));
        if (tasks.isEmpty()) {
            log.debug("[LawProcess] triggerNow versionId={}: no pending task, skip (已被轮询领取或未入队)", versionId);
            return;
        }
        LawProcessTaskDO task = tasks.get(0);
        log.info("[LawProcess] triggerNow versionId={} taskId={} (上传立刻触发)", versionId, task.getId());
        processOneTask(task);
    }

    @Scheduled(fixedDelayString = "${app.process.task-interval-ms:10000}")
    public void processPendingTasks() {
        List<LawProcessTaskDO> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<LawProcessTaskDO>()
                        .eq(LawProcessTaskDO::getProcessStatus, "pending")
                        .lt(LawProcessTaskDO::getRetryCount, maxRetry)
                        .last("LIMIT " + batchSize));
        if (tasks.isEmpty()) return;

        log.debug("[LawProcess] processing {} pending tasks", tasks.size());
        // 受控并发：提交到固定线程池并等待本批完成；CAS 领取保证同一任务不被重复处理。
        List<Future<?>> futures = new ArrayList<>(tasks.size());
        for (LawProcessTaskDO task : tasks) {
            futures.add(executor.submit(() -> processOneTask(task)));
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                log.error("[LawProcess] batch task execution error: {}", e.getMessage(), e);
            }
        }
    }

    private void processOneTask(LawProcessTaskDO task) {
        if (taskMapper.claimTask(task.getId()) == 0) {
            log.debug("[LawProcess] task {} already claimed by another instance, skipping", task.getId());
            return;
        }
        LawProcessingContext ctx = new LawProcessingContext(
                task.getId(), task.getDocumentId(), task.getVersionId(), task.getFileId());
        try {
            runStagesWithLockRetry(ctx, task);
            markDone(task);
        } catch (Exception e) {
            log.error("[LawProcess] task {} (versionId={}) failed (attempt {}): {}",
                    task.getId(), task.getVersionId(), task.getRetryCount() + 1, e.getMessage(), e);
            markFailed(task, e.getMessage());
        }
    }

    /**
     * 跑完整结构化阶段链；对数据库锁竞争（死锁 / 锁等待超时）在事务外短退避后<b>就地重试</b>。
     *
     * <p>死锁是瞬时冲突：MySQL 会回滚其中一方事务，立刻重试通常即可成功。若不就地重试而走
     * {@code markFailed → pending}，任务下一轮又被并发领取、撞同一类锁，导致「重试 N 次全失败」。
     * 各阶段均幂等（写入前清场 / upsert），整链重跑安全；锁竞争之外的异常直接上抛交由失败重试策略处理。
     */
    private void runStagesWithLockRetry(LawProcessingContext ctx, LawProcessTaskDO task) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                for (LawProcessingStage stage : structuralStages) {
                    stage.process(ctx);
                }
                return;
            } catch (PessimisticLockingFailureException e) {
                attempt++;
                if (attempt >= LOCK_RETRY_MAX_ATTEMPTS) {
                    log.warn("[LawProcess] task {} (versionId={}) lock contention persists after {} attempts, giving up to outer retry",
                            task.getId(), task.getVersionId(), attempt);
                    throw e;
                }
                long backoffMs = ThreadLocalRandom.current().nextLong(50L, 200L) * attempt;
                log.warn("[LawProcess] task {} (versionId={}) hit DB lock contention (attempt {}/{}), retry after {}ms: {}",
                        task.getId(), task.getVersionId(), attempt, LOCK_RETRY_MAX_ATTEMPTS, backoffMs, e.getMessage());
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.process.task-interval-ms:10000}")
    public void recoverStuckTasks() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
        List<LawProcessTaskDO> stuckTasks = taskMapper.selectList(
                new LambdaQueryWrapper<LawProcessTaskDO>()
                        .eq(LawProcessTaskDO::getProcessStatus, "processing")
                        .lt(LawProcessTaskDO::getUpdateTime, timeoutThreshold));
        if (stuckTasks.isEmpty()) return;

        log.warn("[LawProcess] found {} stuck processing tasks (timeout={}min)", stuckTasks.size(), processingTimeoutMinutes);
        for (LawProcessTaskDO task : stuckTasks) {
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            if (retries + 1 >= maxRetry) {
                taskMapper.update(null, new LambdaUpdateWrapper<LawProcessTaskDO>()
                        .eq(LawProcessTaskDO::getId, task.getId())
                        .eq(LawProcessTaskDO::getProcessStatus, "processing")
                        .set(LawProcessTaskDO::getProcessStatus, "failed")
                        .set(LawProcessTaskDO::getRetryCount, retries + 1)
                        .set(LawProcessTaskDO::getErrorMessage, "processing timeout, retries exhausted")
                        .set(LawProcessTaskDO::getFinishedAt, LocalDateTime.now()));
                log.error("[LawProcess] task {} timed out and exhausted retries, marked failed", task.getId());
            } else {
                taskMapper.update(null, new LambdaUpdateWrapper<LawProcessTaskDO>()
                        .eq(LawProcessTaskDO::getId, task.getId())
                        .eq(LawProcessTaskDO::getProcessStatus, "processing")
                        .set(LawProcessTaskDO::getProcessStatus, "pending")
                        .set(LawProcessTaskDO::getRetryCount, retries + 1));
                log.warn("[LawProcess] task {} timed out, reset to pending (attempt {})", task.getId(), retries + 1);
            }
        }
    }

    private void markDone(LawProcessTaskDO task) {
        task.setProcessStatus("done");
        task.setErrorMessage(null);
        task.setFinishedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void markFailed(LawProcessTaskDO task, String errorMsg) {
        int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
        task.setRetryCount(retries + 1);
        task.setErrorMessage(errorMsg != null && errorMsg.length() > 500
                ? errorMsg.substring(0, 500) : errorMsg);
        if (retries + 1 >= maxRetry) {
            task.setProcessStatus("failed");
            task.setFinishedAt(LocalDateTime.now());
            log.error("[LawProcess] task {} exhausted retries, marked failed", task.getId());
        } else {
            task.setProcessStatus("pending");
        }
        taskMapper.updateById(task);
    }
}
