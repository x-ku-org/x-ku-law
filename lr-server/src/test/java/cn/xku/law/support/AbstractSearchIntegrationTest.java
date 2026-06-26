package cn.xku.law.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 需要真实 Elasticsearch 的检索集成测试基类：在 {@link AbstractIntegrationTest}（MySQL+Redis）之上
 * 额外起一个 ES 8 容器，并开启 {@code app.search.enabled=true}，激活 ElasticsearchSearchClient。
 */
public abstract class AbstractSearchIntegrationTest extends AbstractIntegrationTest {

    protected static final ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.2"))
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

    static {
        ELASTICSEARCH.start();
    }

    @DynamicPropertySource
    static void registerSearchProperties(DynamicPropertyRegistry registry) {
        registry.add("app.search.enabled", () -> "true");
        registry.add("spring.elasticsearch.uris", () -> "http://" + ELASTICSEARCH.getHttpHostAddress());
    }
}
