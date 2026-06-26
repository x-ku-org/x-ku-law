package cn.xku.law.process.stage;

import cn.xku.law.collect.parser.MetadataOnlyParser;
import cn.xku.law.collect.parser.ParserRegistry;
import cn.xku.law.collect.parser.PlainTextArticleParser;
import cn.xku.law.law.domain.LawArticleSegmentDO;
import cn.xku.law.law.domain.dto.LawArticleCreateDTO;
import cn.xku.law.law.mapper.LawArticleMapper;
import cn.xku.law.law.mapper.LawArticleSegmentMapper;
import cn.xku.law.law.service.LawArticleService;
import cn.xku.law.process.DataGovernanceRecorder;
import cn.xku.law.process.LawProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** SegmentationStage 单测：拆条 + 分片 + 幂等清场（Mockito，无 Spring）。 */
class SegmentationStageTest {

    private LawArticleService articleService;
    private LawArticleMapper articleMapper;
    private LawArticleSegmentMapper segmentMapper;
    private SegmentationStage stage;

    @BeforeEach
    void setUp() {
        ParserRegistry registry = new ParserRegistry(List.of(new PlainTextArticleParser(), new MetadataOnlyParser()));
        articleService = mock(LawArticleService.class);
        articleMapper = mock(LawArticleMapper.class);
        segmentMapper = mock(LawArticleSegmentMapper.class);

        AtomicLong seq = new AtomicLong(0);
        when(articleService.createArticle(any(LawArticleCreateDTO.class)))
                .thenAnswer(inv -> seq.incrementAndGet());

        stage = new SegmentationStage(registry, articleService, articleMapper, segmentMapper,
                mock(DataGovernanceRecorder.class));
        ReflectionTestUtils.setField(stage, "segmentMaxChars", 1000);
    }

    @Test
    void process_splitsArticlesAndCreatesPendingSegments() {
        String text = "第一条 国家保护公民的合法权益，任何组织和个人不得侵犯。"
                + "第二条 本法自公布之日起施行。";
        LawProcessingContext ctx = new LawProcessingContext(1L, 10L, 100L, 5L);
        ctx.setExtractedText(text);

        stage.process(ctx);

        // 幂等清场先于写入
        verify(segmentMapper).physicalDeleteByVersion(100L);
        verify(articleMapper).physicalDeleteByVersion(100L);
        // 两条 → 两次建条款
        verify(articleService, times(2)).createArticle(any(LawArticleCreateDTO.class));

        ArgumentCaptor<LawArticleSegmentDO> captor = ArgumentCaptor.forClass(LawArticleSegmentDO.class);
        verify(segmentMapper, times(2)).insert(captor.capture());
        for (LawArticleSegmentDO seg : captor.getAllValues()) {
            assertThat(seg.getVersionId()).isEqualTo(100L);
            assertThat(seg.getEmbeddingStatus()).isEqualTo("pending");
            assertThat(seg.getSegmentNo()).isEqualTo(1);
            assertThat(seg.getSegmentHash()).isNotBlank();
            assertThat(seg.getSegmentText()).isNotBlank();
        }
    }

    @Test
    void process_unstructuredText_fallsBackToSingleArticle() {
        LawProcessingContext ctx = new LawProcessingContext(2L, 11L, 200L, null);
        ctx.setExtractedText("这是一段没有条款标记的标准说明文本，应退化为单条全文条款用于检索与向量化。");

        stage.process(ctx);

        verify(articleService, times(1)).createArticle(any(LawArticleCreateDTO.class));
        verify(segmentMapper, times(1)).insert(any(LawArticleSegmentDO.class));
    }

    @Test
    void process_noText_skips() {
        LawProcessingContext ctx = new LawProcessingContext(3L, 12L, 300L, 7L);
        // 无 extractedText
        stage.process(ctx);

        verify(segmentMapper, times(0)).physicalDeleteByVersion(any());
        verify(articleService, times(0)).createArticle(any());
    }
}
