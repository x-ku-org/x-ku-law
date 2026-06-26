package cn.xku.law.ai.provider;

import cn.xku.law.ai.config.AiProviderProperties;
import org.springframework.util.StringUtils;

final class AiProviderConfigSupport {

    private AiProviderConfigSupport() {
    }

    static String requiredType(String providerCode, AiProviderProperties.Provider provider) {
        if (!StringUtils.hasText(provider.getType())) {
            throw new IllegalArgumentException("AI provider '" + providerCode + "' must configure type");
        }
        return provider.getType().trim().toLowerCase();
    }

    static String requiredApiKey(String providerCode, AiProviderProperties.Provider provider) {
        if (!StringUtils.hasText(provider.getApiKey())) {
            throw new IllegalArgumentException("AI provider '" + providerCode + "' must configure api-key");
        }
        return provider.getApiKey().trim();
    }

    static String valueOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }
}
