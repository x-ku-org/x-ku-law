package cn.xku.law.ai.provider;

import cn.xku.law.ai.config.AiProviderProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class DashScopeChatModelFactory implements AiChatModelFactory {

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";
    private static final String DEFAULT_MODEL = "qwen-plus";

    @Override
    public boolean supports(String type) {
        return "dashscope".equalsIgnoreCase(type);
    }

    @Override
    public ChatModel create(String providerCode, AiProviderProperties.Provider provider) {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(AiProviderConfigSupport.requiredApiKey(providerCode, provider))
                .baseUrl(AiProviderConfigSupport.valueOrDefault(provider.getBaseUrl(), DEFAULT_BASE_URL))
                .build();

        var options = DashScopeChatOptions.builder()
                .model(AiProviderConfigSupport.valueOrDefault(provider.getModel(), DEFAULT_MODEL));
        if (provider.getTemperature() != null) {
            options.temperature(provider.getTemperature());
        }
        if (provider.getMaxTokens() != null) {
            options.maxToken(provider.getMaxTokens());
        }

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options.build())
                .build();
    }
}
