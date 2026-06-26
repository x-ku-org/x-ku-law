package cn.xku.law.process.stage;

import cn.xku.law.ai.provider.AiChatModelRegistry;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.service.LawDocumentService;
import cn.xku.law.law.service.LawTagAttacher;
import cn.xku.law.process.DataGovernanceRecorder;
import cn.xku.law.process.LawProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** MetadataEnrichmentStage 单测：有 provider 时写 summary+tags / 无 provider 优雅跳过 / 无正文跳过（Mockito，无 Spring）。 */
class MetadataEnrichmentStageTest {

    private AiChatModelRegistry registry;
    private LawDocumentService documentService;
    private LawTagAttacher tagAttacher;
    private MetadataEnrichmentStage stage;

    @BeforeEach
    void setUp() {
        registry = mock(AiChatModelRegistry.class);
        documentService = mock(LawDocumentService.class);
        tagAttacher = mock(LawTagAttacher.class);
        stage = new MetadataEnrichmentStage(registry, documentService, tagAttacher,
                mock(DataGovernanceRecorder.class));
        ReflectionTestUtils.setField(stage, "maxInputChars", 6000);
    }

    private LawProcessingContext ctx(long versionId, long documentId) {
        LawProcessingContext c = new LawProcessingContext(1L, documentId, versionId, null);
        c.setExtractedText("第一条 为保护个人信息权益，规范个人信息处理活动，制定本法。");
        return c;
    }

    @Test
    void providerAvailable_writesSummaryAndTags() {
        ChatModel chatModel = mock(ChatModel.class);
        when(registry.getDefaultChatModel()).thenReturn(chatModel);
        when(chatModel.call(anyString())).thenReturn(
                "```json\n{\"summary\":\"本法规范个人信息处理活动，保护个人信息权益。\",\"tags\":[\"个人信息\",\"数据安全\",\"个人信息\"]}\n```");
        when(documentService.getById(10L)).thenReturn(new LawDocumentDO());

        stage.process(ctx(100L, 10L));

        ArgumentCaptor<LawDocumentDO> captor = ArgumentCaptor.forClass(LawDocumentDO.class);
        verify(documentService).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getSummary()).isEqualTo("本法规范个人信息处理活动，保护个人信息权益。");
        // 去重后只剩两个标签
        verify(tagAttacher).attach(eq(10L), eq("个人信息"));
        verify(tagAttacher).attach(eq(10L), eq("数据安全"));
        verify(tagAttacher, times(2)).attach(eq(10L), anyString());
    }

    @Test
    void existingSummary_notOverwritten_butTagsStillAttached() {
        ChatModel chatModel = mock(ChatModel.class);
        when(registry.getDefaultChatModel()).thenReturn(chatModel);
        when(chatModel.call(anyString())).thenReturn("{\"summary\":\"AI摘要\",\"tags\":[\"标签A\"]}");
        LawDocumentDO existing = new LawDocumentDO();
        existing.setSummary("人工已写摘要");
        when(documentService.getById(10L)).thenReturn(existing);

        stage.process(ctx(100L, 10L));

        verify(documentService, never()).updateById(any());
        verify(tagAttacher).attach(eq(10L), eq("标签A"));
    }

    @Test
    void noProvider_skipsGracefully() {
        when(registry.getDefaultChatModel())
                .thenThrow(new IllegalArgumentException("AI chat provider is not available: openai"));

        stage.process(ctx(100L, 10L));

        verify(documentService, never()).updateById(any());
        verify(tagAttacher, never()).attach(any(), anyString());
    }

    @Test
    void noExtractedText_skips() {
        LawProcessingContext c = new LawProcessingContext(1L, 10L, 100L, null);
        // 无 extractedText
        stage.process(c);

        verify(registry, never()).getDefaultChatModel();
        verify(documentService, never()).updateById(any());
    }
}
