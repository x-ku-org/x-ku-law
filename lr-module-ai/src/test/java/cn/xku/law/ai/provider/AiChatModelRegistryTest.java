package cn.xku.law.ai.provider;

import cn.xku.law.ai.config.AiChatModelConfiguration;
import cn.xku.law.ai.config.AiProviderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiChatModelRegistryTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues("spring.ai.model.chat=none");

    @Test
    void bindsOpenProviderMapAndRoutesEnabledProviders() {
        contextRunner
                .withPropertyValues(
                        "app.ai.default-provider=custom-provider",
                        "app.ai.providers.custom-provider.enabled=true",
                        "app.ai.providers.custom-provider.type=fake",
                        "app.ai.providers.custom-provider.api-key=test-key",
                        "app.ai.providers.custom-provider.model=test-model",
                        "app.ai.providers.disabled-provider.enabled=false",
                        "app.ai.providers.disabled-provider.type=fake",
                        "app.ai.providers.disabled-provider.api-key=test-key")
                .run(context -> {
                    AiProviderProperties properties = context.getBean(AiProviderProperties.class);
                    assertThat(properties.getProviders()).containsKeys("custom-provider", "disabled-provider");
                    assertThat(properties.getProviders().get("custom-provider").getModel()).isEqualTo("test-model");

                    AiChatModelRegistry registry = context.getBean(AiChatModelRegistry.class);
                    assertThat(registry.getEnabledProviders()).containsExactly("custom-provider");
                    assertThat(registry.getDefaultChatModel()).isInstanceOf(StubChatModel.class);
                    assertThatThrownBy(() -> registry.getChatModel("disabled-provider"))
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("disabled-provider");
                });
    }

    @Test
    void skipsProvidersWithoutApiKeySoApplicationCanStartWithoutSecrets() {
        contextRunner
                .withPropertyValues(
                        "app.ai.default-provider=openai",
                        "app.ai.providers.openai.enabled=true",
                        "app.ai.providers.openai.type=fake",
                        "app.ai.providers.openai.api-key=")
                .run(context -> {
                    AiChatModelRegistry registry = context.getBean(AiChatModelRegistry.class);
                    assertThat(registry.getEnabledProviders()).isEmpty();
                    assertThatThrownBy(registry::getDefaultChatModel)
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("openai");
                });
    }

    @Test
    void failsFastForUnknownEnabledProviderType() {
        contextRunner
                .withPropertyValues(
                        "app.ai.providers.custom.enabled=true",
                        "app.ai.providers.custom.type=unknown",
                        "app.ai.providers.custom.api-key=test-key")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration
    @Import(AiChatModelConfiguration.class)
    @EnableConfigurationProperties(AiProviderProperties.class)
    static class TestConfig {

        @Bean
        AiChatModelFactory fakeAiChatModelFactory() {
            return new AiChatModelFactory() {
                @Override
                public boolean supports(String type) {
                    return "fake".equals(type);
                }

                @Override
                public ChatModel create(String providerCode, AiProviderProperties.Provider provider) {
                    return new StubChatModel();
                }
            };
        }
    }

    static class StubChatModel implements ChatModel {
        @Override
        public ChatResponse call(Prompt prompt) {
            throw new UnsupportedOperationException("stub");
        }
    }
}
