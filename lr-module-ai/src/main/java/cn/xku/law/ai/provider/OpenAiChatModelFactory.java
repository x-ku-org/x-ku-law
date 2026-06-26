package cn.xku.law.ai.provider;

import cn.xku.law.ai.config.AiProviderProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class OpenAiChatModelFactory implements AiChatModelFactory {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o";

    @Override
    public boolean supports(String type) {
        return "openai".equalsIgnoreCase(type) || "openai-compatible".equalsIgnoreCase(type);
    }

    @Override
    public ChatModel create(String providerCode, AiProviderProperties.Provider provider) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(AiProviderConfigSupport.requiredApiKey(providerCode, provider))
                .baseUrl(AiProviderConfigSupport.valueOrDefault(provider.getBaseUrl(), DEFAULT_BASE_URL))
                .completionsPath(AiProviderConfigSupport.valueOrDefault(
                        provider.getCompletionsPath(), DEFAULT_COMPLETIONS_PATH))
                .build();

        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .model(AiProviderConfigSupport.valueOrDefault(provider.getModel(), DEFAULT_MODEL));
        if (provider.getTemperature() != null) {
            options.temperature(provider.getTemperature());
        }
        if (provider.getMaxTokens() != null) {
            options.maxTokens(provider.getMaxTokens());
        }

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options.build())
                .build();
    }
}
