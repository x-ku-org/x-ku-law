package cn.xku.law.ai.provider;

import cn.xku.law.ai.config.AiProviderProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekChatModelFactory implements AiChatModelFactory {

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_COMPLETIONS_PATH = "/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-chat";

    @Override
    public boolean supports(String type) {
        return "deepseek".equalsIgnoreCase(type);
    }

    @Override
    public ChatModel create(String providerCode, AiProviderProperties.Provider provider) {
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(AiProviderConfigSupport.requiredApiKey(providerCode, provider))
                .baseUrl(AiProviderConfigSupport.valueOrDefault(provider.getBaseUrl(), DEFAULT_BASE_URL))
                .completionsPath(AiProviderConfigSupport.valueOrDefault(
                        provider.getCompletionsPath(), DEFAULT_COMPLETIONS_PATH))
                .build();

        DeepSeekChatOptions.Builder options = DeepSeekChatOptions.builder()
                .model(AiProviderConfigSupport.valueOrDefault(provider.getModel(), DEFAULT_MODEL));
        if (provider.getTemperature() != null) {
            options.temperature(provider.getTemperature());
        }
        if (provider.getMaxTokens() != null) {
            options.maxTokens(provider.getMaxTokens());
        }

        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .defaultOptions(options.build())
                .build();
    }
}
