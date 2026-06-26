package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.domain.LawDocumentTagDO;
import cn.xku.law.law.domain.TagDO;
import cn.xku.law.law.mapper.LawDocumentTagMapper;
import cn.xku.law.law.mapper.TagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** LawTagAttacher 单测：取或建标签 + 关联去重（Mockito，无 Spring）。 */
class LawTagAttacherTest {

    private TagMapper tagMapper;
    private LawDocumentTagMapper documentTagMapper;
    private LawTagAttacherImpl attacher;

    @BeforeEach
    void setUp() {
        tagMapper = mock(TagMapper.class);
        documentTagMapper = mock(LawDocumentTagMapper.class);
        attacher = new LawTagAttacherImpl(tagMapper, documentTagMapper);
    }

    @Test
    void newTag_isCreated_andLinked() {
        when(tagMapper.selectOne(any())).thenReturn(null);
        when(tagMapper.insert(any(TagDO.class))).thenAnswer(inv -> {
            ((TagDO) inv.getArgument(0)).setId(7L);
            return 1;
        });
        when(documentTagMapper.selectCount(any())).thenReturn(0L);

        attacher.attach(10L, "数据安全");

        ArgumentCaptor<TagDO> tagCaptor = ArgumentCaptor.forClass(TagDO.class);
        verify(tagMapper).insert(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getTagName()).isEqualTo("数据安全");
        assertThat(tagCaptor.getValue().getTagType()).isEqualTo("law");
        assertThat(tagCaptor.getValue().getTagCode()).startsWith("law:");
        assertThat(tagCaptor.getValue().getStatus()).isEqualTo("enabled");

        ArgumentCaptor<LawDocumentTagDO> linkCaptor = ArgumentCaptor.forClass(LawDocumentTagDO.class);
        verify(documentTagMapper).insert(linkCaptor.capture());
        assertThat(linkCaptor.getValue().getDocumentId()).isEqualTo(10L);
        assertThat(linkCaptor.getValue().getTagId()).isEqualTo(7L);
    }

    @Test
    void existingTag_reused_notRecreated() {
        TagDO existing = new TagDO();
        existing.setId(42L);
        when(tagMapper.selectOne(any())).thenReturn(existing);
        when(documentTagMapper.selectCount(any())).thenReturn(0L);

        attacher.attach(10L, "行政许可");

        verify(tagMapper, never()).insert(any(TagDO.class));
        ArgumentCaptor<LawDocumentTagDO> linkCaptor = ArgumentCaptor.forClass(LawDocumentTagDO.class);
        verify(documentTagMapper).insert(linkCaptor.capture());
        assertThat(linkCaptor.getValue().getTagId()).isEqualTo(42L);
    }

    @Test
    void existingLink_notDuplicated() {
        TagDO existing = new TagDO();
        existing.setId(42L);
        when(tagMapper.selectOne(any())).thenReturn(existing);
        when(documentTagMapper.selectCount(any())).thenReturn(1L);

        attacher.attach(10L, "行政许可");

        verify(documentTagMapper, never()).insert(any(LawDocumentTagDO.class));
    }

    @Test
    void blankName_ignored() {
        attacher.attach(10L, "   ");
        verify(tagMapper, never()).selectOne(any());
        verify(documentTagMapper, never()).insert(any(LawDocumentTagDO.class));
    }

    @Test
    void sameNameDifferentType_distinctTagCodes() {
        when(tagMapper.selectOne(any())).thenReturn(null);
        when(tagMapper.insert(any(TagDO.class))).thenAnswer(inv -> {
            ((TagDO) inv.getArgument(0)).setId(1L);
            return 1;
        });
        when(documentTagMapper.selectCount(any())).thenReturn(0L);

        attacher.attach(10L, "law", "同名");
        attacher.attach(10L, "topic", "同名");

        ArgumentCaptor<TagDO> captor = ArgumentCaptor.forClass(TagDO.class);
        verify(tagMapper, times(2)).insert(captor.capture());
        String lawCode = captor.getAllValues().get(0).getTagCode();
        String topicCode = captor.getAllValues().get(1).getTagCode();
        assertThat(lawCode).startsWith("law:");
        assertThat(topicCode).startsWith("topic:");
        assertThat(lawCode).isNotEqualTo(topicCode);
    }
}
