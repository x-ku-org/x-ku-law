package cn.xku.law.process.embed;

import cn.xku.law.common.client.EmbeddingClient;
import cn.xku.law.common.exception.EmbeddingUnavailableException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 调用 OpenAI 兼容 {@code /v1/embeddings} 接口的真实嵌入客户端。
 * 仅在 {@code app.vector.embedding.enabled=true} 时装配；否则由 {@code NoOpEmbeddingClient} 兜底
 * （后者 @ConditionalOnMissingBean，一旦本 Bean 存在即退让）。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.vector.embedding", name = "enabled", havingValue = "true")
public class RestClientEmbeddingClient implements EmbeddingClient {

    private final RestClient restClient;
    private final String embeddingsPath;
    private final String model;
    private final int dimension;

    public RestClientEmbeddingClient(
            @Value("${app.vector.embedding.base-url:https://api.openai.com}") String baseUrl,
            @Value("${app.vector.embedding.embeddings-path:/v1/embeddings}") String embeddingsPath,
            @Value("${app.vector.embedding.api-key:}") String apiKey,
            @Value("${app.vector.embedding.model:text-embedding-3-small}") String model,
            @Value("${app.vector.dimension:1536}") int dimension) {
        this.embeddingsPath = embeddingsPath;
        this.model = model;
        this.dimension = dimension;
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (StringUtils.hasText(apiKey)) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        this.restClient = builder.build();
        log.info("[Embedding] RestClientEmbeddingClient enabled: baseUrl={}, model={}, dim={}", baseUrl, model, dimension);
    }

    @Override
    public float[] embed(String text) {
        List<float[]> result = embedBatch(List.of(text == null ? "" : text));
        if (result.isEmpty()) {
            throw new EmbeddingUnavailableException("嵌入返回为空");
        }
        return result.get(0);
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        try {
            EmbeddingResponse resp = restClient.post()
                    .uri(embeddingsPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", model, "input", texts))
                    .retrieve()
                    .body(EmbeddingResponse.class);
            if (resp == null || resp.data() == null || resp.data().isEmpty()) {
                throw new EmbeddingUnavailableException("嵌入服务返回为空");
            }
            return resp.data().stream().map(EmbeddingItem::embedding).toList();
        } catch (EmbeddingUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingUnavailableException("嵌入服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingResponse(List<EmbeddingItem> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingItem(float[] embedding) {
    }
}
