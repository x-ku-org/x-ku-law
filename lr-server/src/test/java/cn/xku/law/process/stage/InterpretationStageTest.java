package cn.xku.law.process.stage;

import cn.xku.law.ai.provider.AiChatModelRegistry;
import cn.xku.law.law.service.LawInterpretationService;
import cn.xku.law.process.LawProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InterpretationStage 单测：有 provider 时整篇解读落库 / 无 provider 抛出（旁路任务可见重试）/
 * 无正文跳过 / 模型空返回不落库（Mockito，无 Spring）。
 */
class InterpretationStageTest {

    private AiChatModelRegistry registry;
    private LawInterpretationService interpretationService;
    private InterpretationStage stage;

    @BeforeEach
    void setUp() {
        registry = mock(AiChatModelRegistry.class);
        interpretationService = mock(LawInterpretationService.class);
        stage = new InterpretationStage(registry, interpretationService);
        ReflectionTestUtils.setField(stage, "maxInputChars", 8000);
        ReflectionTestUtils.setField(stage, "defaultProvider", "openai");
    }

    private LawProcessingContext ctx(long versionId, long documentId, String text) {
        LawProcessingContext c = new LawProcessingContext(1L, documentId, versionId, null);
        c.setExtractedText(text);
        return c;
    }

    @Test
    void providerAvailable_savesInterpretation() {
        ChatModel chatModel = mock(ChatModel.class);
        when(registry.getDefaultChatModel()).thenReturn(chatModel);
        when(chatModel.call(anyString())).thenReturn("## 立法目的\n本法规范……");

        stage.process(ctx(100L, 10L, "第一条 为保护个人信息权益，制定本法。"));

        verify(interpretationService).saveForVersion(eq(10L), eq(100L), eq("openai"), eq("## 立法目的\n本法规范……"));
    }

    @Test
    void noProvider_throws() {
        when(registry.getDefaultChatModel())
                .thenThrow(new IllegalArgumentException("AI chat provider is not available: openai"));

        assertThatThrownBy(() -> stage.process(ctx(100L, 10L, "正文")))
                .isInstanceOf(RuntimeException.class);
        verify(interpretationService, never()).saveForVersion(any(), any(), anyString(), anyString());
    }

    @Test
    void noText_skips() {
        LawProcessingContext c = new LawProcessingContext(1L, 10L, 100L, null);
        stage.process(c);

        verify(registry, never()).getDefaultChatModel();
        verify(interpretationService, never()).saveForVersion(any(), any(), anyString(), anyString());
    }

    @Test
    void emptyResponse_skipsSave() {
        ChatModel chatModel = mock(ChatModel.class);
        when(registry.getDefaultChatModel()).thenReturn(chatModel);
        when(chatModel.call(anyString())).thenReturn("   ");

        stage.process(ctx(100L, 10L, "正文"));

        verify(interpretationService, never()).saveForVersion(any(), any(), anyString(), anyString());
    }
}
