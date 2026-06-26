package cn.xku.law.common.client.noop;

import cn.xku.law.common.client.EmbeddingClient;
import cn.xku.law.common.exception.EmbeddingUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * EmbeddingClient 空实现，未接入真实嵌入模型时自动激活，防止启动失败。
 * 通过 {@code app.vector.embedding.enabled!=true}（默认/缺省即生效）与
 * {@code RestClientEmbeddingClient} 的 @ConditionalOnProperty（havingValue=true）严格互补，
 * 按同一开关清晰二选一，确保任意时刻恰好装配一个 EmbeddingClient。
 *
 * <p>注意：本类是被组件扫描的 {@code @Component}，不能再叠加 {@code @ConditionalOnMissingBean(EmbeddingClient.class)} —
 * 该条件在扫描期会匹配到“自身”的 bean 定义而把自己排除掉，导致最终没有任何 EmbeddingClient
 * （上下文因 LawChatAgentService 等装配失败而起不来）。互斥已由上述属性开关保证，无需再兜底。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.vector.embedding", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmbeddingClient implements EmbeddingClient {

    @Value("${app.vector.dimension:1536}")
    private int dimension;

    @Override
    public float[] embed(String text) {
        log.warn("[NoOpEmbeddingClient] embed called — 嵌入模型未接入");
        throw new EmbeddingUnavailableException("嵌入服务未启用：未配置真实 EmbeddingClient");
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        log.warn("[NoOpEmbeddingClient] embedBatch called — 嵌入模型未接入，size={}", texts == null ? 0 : texts.size());
        throw new EmbeddingUnavailableException("嵌入服务未启用：未配置真实 EmbeddingClient");
    }

    @Override
    public int dimension() {
        return dimension;
    }
}
