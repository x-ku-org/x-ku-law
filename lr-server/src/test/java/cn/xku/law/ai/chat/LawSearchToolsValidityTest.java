package cn.xku.law.ai.chat;

import cn.xku.law.law.domain.LawDocumentDO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 校验语义检索的时效判定：废止/失效→repealed，历史版本→superseded，现行→current。 */
class LawSearchToolsValidityTest {

    private LawDocumentDO doc(String status, Long currentVersionId) {
        LawDocumentDO d = new LawDocumentDO();
        d.setStatus(status);
        d.setCurrentVersionId(currentVersionId);
        return d;
    }

    @Test
    void nullDocIsCurrent() {
        assertThat(LawSearchTools.validityOf(null, 1L)).isEqualTo("current");
    }

    @Test
    void repealedDocIsRepealed() {
        assertThat(LawSearchTools.validityOf(doc("repealed", 5L), 5L)).isEqualTo("repealed");
    }

    @Test
    void expiredDocIsRepealed() {
        assertThat(LawSearchTools.validityOf(doc("expired", 5L), 5L)).isEqualTo("repealed");
    }

    @Test
    void amendedDocIsSuperseded() {
        assertThat(LawSearchTools.validityOf(doc("amended", 5L), 5L)).isEqualTo("superseded");
    }

    @Test
    void nonCurrentVersionIsSuperseded() {
        // 文档现行有效，但被引用的是旧版本（与 currentVersionId 不一致）
        assertThat(LawSearchTools.validityOf(doc("effective", 9L), 3L)).isEqualTo("superseded");
    }

    @Test
    void currentVersionEffectiveIsCurrent() {
        assertThat(LawSearchTools.validityOf(doc("effective", 9L), 9L)).isEqualTo("current");
    }
}
