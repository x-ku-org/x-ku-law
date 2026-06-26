package cn.xku.law.ai.provider;

import cn.xku.law.ai.config.AiProviderProperties;
import org.springframework.ai.chat.model.ChatModel;

public interface AiChatModelFactory {

    boolean supports(String type);

    ChatModel create(String providerCode, AiProviderProperties.Provider provider);
}
