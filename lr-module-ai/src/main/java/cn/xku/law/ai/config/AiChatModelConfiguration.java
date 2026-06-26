package cn.xku.law.ai.config;

import cn.xku.law.ai.provider.AiChatModelFactory;
import cn.xku.law.ai.provider.AiChatModelRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiChatModelConfiguration {

    @Bean
    public AiChatModelRegistry aiChatModelRegistry(
            AiProviderProperties properties,
            List<AiChatModelFactory> factories) {
        Map<String, ChatModel> chatModels = new LinkedHashMap<>();
        properties.getProviders().forEach((providerCode, provider) -> {
            if (!isEnabledWithApiKey(provider)) {
                return;
            }
            if (!StringUtils.hasText(provider.getType())) {
                throw new IllegalArgumentException(
                        "AI provider '" + providerCode + "' must configure type");
            }
            String type = provider.getType().trim().toLowerCase();
            AiChatModelFactory factory = factories.stream()
                    .filter(candidate -> candidate.supports(type))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unsupported AI provider type '" + provider.getType()
                                    + "' for provider '" + providerCode + "'"));
            chatModels.put(providerCode, factory.create(providerCode, provider));
        });
        return new AiChatModelRegistry(properties.getDefaultProvider(), chatModels);
    }

    private boolean isEnabledWithApiKey(AiProviderProperties.Provider provider) {
        return Boolean.TRUE.equals(provider.getEnabled())
                && StringUtils.hasText(provider.getApiKey());
    }
}
