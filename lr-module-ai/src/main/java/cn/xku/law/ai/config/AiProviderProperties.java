package cn.xku.law.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProviderProperties {

    private String defaultProvider = "openai";

    private Map<String, Provider> providers = new LinkedHashMap<>();

    @Data
    public static class Provider {

        private Boolean enabled = true;

        /**
         * Adapter type. Built-in values: openai, openai-compatible, deepseek, dashscope.
         */
        private String type;

        private String apiKey;

        private String baseUrl;

        private String completionsPath;

        private String model;

        private Double temperature;

        private Integer maxTokens;
    }
}
