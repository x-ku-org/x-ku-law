package cn.xku.law.process;

/**
 * 法规处理管线的一个阶段。实现类注册为 Spring Bean 即自动纳入管线，由
 * {@link LawProcessTaskProcessor} 按 {@link #order()} 升序依次执行。
 *
 * <p>新增解读 / 变更分析等阶段：实现本接口并标注 {@code @Component} 即可，无需改动编排器。
 * 各阶段应保证幂等（任务失败会整体重跑）：写入前先清理本版本的旧产出，或采用 upsert。
 */
public interface LawProcessingStage {

    /** 阶段名，用于日志 */
    String name();

    /** 执行顺序，升序。约定：提取 10、分段 20、富集 25、发布 30、解读 40、变更分析 50、AI入队 60。 */
    int order();

    /**
     * 本阶段是否依赖 AI 模型（LLM）。默认否。
     *
     * <p>结构化先行 / AI 旁路：标记为 {@code true} 的阶段（元数据富集、解读）<b>不在</b>结构化主管线内联执行，
     * 而是由 {@code AiTaskEnqueueStage} 入队、{@code LawAiTaskProcessor} 单独消费。
     * 这样 2 万+ 文件的结构化回填（提取/分段/发布/向量/对比）完全不触发 LLM，
     * AI（摘要/标签/解读）成为可单独开关（{@code app.process.ai.enabled}）、可受限并发的旁路扫描。
     */
    default boolean requiresAi() {
        return false;
    }

    /** 执行本阶段；抛出异常将使整条任务标记失败并按重试策略重跑。 */
    void process(LawProcessingContext ctx) throws Exception;
}
