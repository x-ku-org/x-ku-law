package cn.xku.law.ai.chat;

import cn.xku.law.ai.domain.AiCitationDO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 校验 AI 答案风险分级：失效/废止引用、低置信、无有效引用 → warning，否则 normal。 */
class RiskLevelTest {

    private AiCitationDO cite(boolean verified, String validity, Double confidence) {
        AiCitationDO c = new AiCitationDO();
        c.setVerifiedFlag(verified);
        c.setValidityStatus(validity);
        c.setConfidenceScore(confidence == null ? null : BigDecimal.valueOf(confidence));
        return c;
    }

    @Test
    void noCitationsIsWarning() {
        assertThat(LawChatAgentService.computeRiskLevel("答案无引用", List.of())).isEqualTo("warning");
    }

    @Test
    void noVerifiedCitationIsWarning() {
        // 召回但正文未引用（verifiedFlag 全 false）→ 依据不足
        assertThat(LawChatAgentService.computeRiskLevel("答案", List.of(cite(false, "current", 90.0))))
                .isEqualTo("warning");
    }

    @Test
    void repealedCitationIsWarning() {
        assertThat(LawChatAgentService.computeRiskLevel("答案[1]", List.of(cite(true, "repealed", 90.0))))
                .isEqualTo("warning");
    }

    @Test
    void supersededCitationIsWarning() {
        assertThat(LawChatAgentService.computeRiskLevel("答案[1]", List.of(cite(true, "superseded", 90.0))))
                .isEqualTo("warning");
    }

    @Test
    void lowConfidenceIsWarning() {
        assertThat(LawChatAgentService.computeRiskLevel("答案[1]", List.of(cite(true, "current", 30.0))))
                .isEqualTo("warning");
    }

    @Test
    void verifiedCurrentHighConfidenceIsNormal() {
        assertThat(LawChatAgentService.computeRiskLevel("答案[1]", List.of(cite(true, "current", 80.0))))
                .isEqualTo("normal");
    }

    @Test
    void readOriginalNullConfidenceIsNormal() {
        // read_law_articles 登记的引用 confidence=null（直读原文，高信任），不应误判为低置信
        assertThat(LawChatAgentService.computeRiskLevel("答案[1]", List.of(cite(true, "current", null))))
                .isEqualTo("normal");
    }
}
