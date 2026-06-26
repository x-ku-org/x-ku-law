package cn.xku.law.ops;

/**
 * 定时任务当前配置（只读）。值来自 application.yml / 环境变量，改动需重启生效。
 *
 * @param key          任务标识（collect/process/search/vector）
 * @param name         任务名称
 * @param enabled      是否启用
 * @param scheduleType 调度类型：cron / interval
 * @param schedule     cron 表达式或轮询间隔描述
 * @param maxRetry     最大重试次数（无则 null）
 * @param configKeys   对应的可配置项 / 环境变量提示
 * @param note         补充说明
 */
public record OpsConfigVO(
        String key,
        String name,
        boolean enabled,
        String scheduleType,
        String schedule,
        Integer maxRetry,
        String configKeys,
        String note
) {}
