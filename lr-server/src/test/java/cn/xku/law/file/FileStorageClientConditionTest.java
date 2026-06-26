package cn.xku.law.file;

import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.common.client.noop.NoOpFileStorageClient;
import cn.xku.law.config.StorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class FileStorageClientConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StorageClientConfig.class);

    @Test
    void registersOnlyMinioClientWhenEndpointIsConfigured() {
        contextRunner
                .withPropertyValues(
                        "app.storage.endpoint=http://localhost:9000",
                        "app.storage.bucket=lr-law",
                        "app.storage.access-key=minioadmin",
                        "app.storage.secret-key=minioadmin")
                .run(context -> {
                    assertThat(context).hasSingleBean(FileStorageClient.class);
                    assertThat(context).hasSingleBean(MinioFileStorageClient.class);
                    assertThat(context).doesNotHaveBean(NoOpFileStorageClient.class);
                    assertThat(context.getBeanNamesForType(FileStorageClient.class))
                            .containsExactly("ossFileStorageClient");
                });
    }

    @Test
    void registersOnlyNoOpClientWhenEndpointIsBlank() {
        contextRunner
                .withPropertyValues("app.storage.endpoint=")
                .run(this::assertOnlyNoOpClient);
    }

    @Test
    void registersOnlyNoOpClientWhenEndpointIsMissing() {
        contextRunner
                .run(this::assertOnlyNoOpClient);
    }

    private void assertOnlyNoOpClient(org.springframework.boot.test.context.assertj.AssertableApplicationContext context) {
        assertThat(context).hasSingleBean(FileStorageClient.class);
        assertThat(context).hasSingleBean(NoOpFileStorageClient.class);
        assertThat(context).doesNotHaveBean(MinioFileStorageClient.class);
        assertThat(context.getBeanNamesForType(FileStorageClient.class))
                .containsExactly("noOpFileStorageClient");
    }

    @Import({MinioFileStorageClient.class, NoOpFileStorageClient.class})
    @EnableConfigurationProperties(StorageProperties.class)
    static class StorageClientConfig {
    }
}
