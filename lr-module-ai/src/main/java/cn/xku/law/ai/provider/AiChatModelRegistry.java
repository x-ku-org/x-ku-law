package cn.xku.law.ai.provider;

import org.springframework.ai.chat.model.ChatModel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AiChatModelRegistry {

    private final String defaultProvider;
    private final Map<String, ChatModel> chatModels;

    public AiChatModelRegistry(String defaultProvider, Map<String, ChatModel> chatModels) {
        this.defaultProvider = defaultProvider;
        this.chatModels = Collections.unmodifiableMap(new LinkedHashMap<>(chatModels));
    }

    public ChatModel getDefaultChatModel() {
        return getChatModel(defaultProvider);
    }

    public ChatModel getChatModel(String providerCode) {
        ChatModel chatModel = chatModels.get(providerCode);
        if (chatModel == null) {
            throw new IllegalArgumentException("AI chat provider is not available: " + providerCode);
        }
        return chatModel;
    }

    public Set<String> getEnabledProviders() {
        return chatModels.keySet();
    }

    public boolean hasProvider(String providerCode) {
        return chatModels.containsKey(providerCode);
    }
}
