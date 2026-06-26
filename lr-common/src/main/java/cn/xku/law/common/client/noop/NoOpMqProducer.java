package cn.xku.law.common.client.noop;

import cn.xku.law.common.client.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** MqProducer 空实现，MQ 未接入时自动激活 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "rabbitMqProducer")
public class NoOpMqProducer implements MqProducer {

    @Override
    public void send(String exchange, String routingKey, Object payload) {
        log.warn("[NoOpMqProducer] send called — MQ not configured. exchange={}, routingKey={}", exchange, routingKey);
    }

    @Override
    public void sendDelay(String exchange, String routingKey, Object payload, long delayMs) {
        log.warn("[NoOpMqProducer] sendDelay called — MQ not configured. exchange={}, routingKey={}, delayMs={}",
                exchange, routingKey, delayMs);
    }
}
