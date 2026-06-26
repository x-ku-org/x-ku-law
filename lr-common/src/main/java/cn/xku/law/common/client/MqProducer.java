package cn.xku.law.common.client;

/**
 * 消息队列生产者接口（RabbitMQ 统一抽象）。
 * TODO: 在 lr-server 中提供 RabbitTemplate 具体实现。
 */
public interface MqProducer {

    /**
     * 发送消息到指定 exchange/routingKey。
     *
     * @param exchange   Exchange 名称
     * @param routingKey Routing Key
     * @param payload    消息体（JSON 字符串或可序列化对象）
     */
    void send(String exchange, String routingKey, Object payload);

    /**
     * 发送延迟消息（需 RabbitMQ 延迟消息插件支持）。
     *
     * @param exchange   Exchange 名称
     * @param routingKey Routing Key
     * @param payload    消息体
     * @param delayMs    延迟毫秒数
     */
    void sendDelay(String exchange, String routingKey, Object payload, long delayMs);
}
