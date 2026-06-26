package cn.xku.law.search;

import cn.xku.law.common.client.SearchClient;
import cn.xku.law.support.AbstractSearchIntegrationTest;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 向量 kNN 真实路径集成测试：用真实 ES（Testcontainers），手工写入带 embedding 的分片文档，
 * 验证 ElasticsearchSearchClient 按 dense_vector 映射建索引、vectorSearch 返回正确的最近邻顺序。
 * 不依赖嵌入模型（向量手造），只验证 ES 这一侧的真实代码。
 */
class VectorSearchIT extends AbstractSearchIntegrationTest {

    private static final String INDEX = "law_segment";

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private ElasticsearchClient esClient;

    @DynamicPropertySource
    static void registerVectorProperties(DynamicPropertyRegistry registry) {
        registry.add("app.vector.enabled", () -> "true");
        registry.add("app.vector.dimension", () -> "4");
    }

    private void indexSegment(long segmentId, float[] embedding) {
        Map<String, Object> source = new HashMap<>();
        source.put("segmentId", segmentId);
        source.put("articleId", 1L);
        source.put("versionId", 1L);
        source.put("documentId", 1L);
        source.put("segmentText", "segment-" + segmentId);
        source.put("tenantId", 0L);
        source.put("isPublic", true);
        source.put("embedding", embedding);
        searchClient.indexDocument(INDEX, String.valueOf(segmentId), source);
    }

    @Test
    void vectorSearchReturnsNearestNeighborsInOrder() throws Exception {
        indexSegment(1L, new float[]{1f, 0f, 0f, 0f});
        indexSegment(2L, new float[]{0f, 1f, 0f, 0f});
        indexSegment(3L, new float[]{0.9f, 0.1f, 0f, 0f});
        esClient.indices().refresh(r -> r.index(INDEX));

        List<String> hits = searchClient.vectorSearch(INDEX, new float[]{1f, 0f, 0f, 0f}, 2);

        // 与 [1,0,0,0] 余弦最近的是 1，其次 3；2（正交）应被 top-2 排除
        assertThat(hits).containsExactly("1", "3");
    }
}
