package cn.xku.law.subscription.serviceImpl;

import cn.xku.law.subscription.domain.DocumentMatchContext;
import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 校验订阅规则条件匹配：仅命中规则关键词/地区/效力级别的法规才生成命中（evaluate 返回非 null）。 */
class SubscriptionRuleMatchTest {

    // evaluate 只用到 objectMapper 字段，其余依赖传 null 即可。
    private final SubscriptionRuleServiceImpl service =
            new SubscriptionRuleServiceImpl(null, null, null, new ObjectMapper());

    private SubscriptionRuleDO rule(String keyword, String filtersJson) {
        SubscriptionRuleDO r = new SubscriptionRuleDO();
        r.setRuleName("测试规则");
        r.setKeyword(keyword);
        r.setFiltersJson(filtersJson);
        return r;
    }

    private DocumentMatchContext doc(String title, String legalLevel, String regionCode) {
        DocumentMatchContext ctx = new DocumentMatchContext();
        ctx.setMatchType("update");
        ctx.setTitle(title);
        ctx.setLegalLevel(legalLevel);
        ctx.setRegionCode(regionCode);
        return ctx;
    }

    @Test
    void keywordHitOnTitle() {
        assertThat(service.evaluate(rule("数据出境", null), doc("数据出境安全评估办法", "部门规章", null)))
                .isNotNull();
    }

    @Test
    void keywordMissReturnsNull() {
        assertThat(service.evaluate(rule("数据出境", null), doc("劳动合同法", "法律", null)))
                .isNull();
    }

    @Test
    void effectLevelFilterHit() {
        assertThat(service.evaluate(rule(null, "{\"effectLevel\":[\"行政法规\"]}"),
                doc("某条例", "行政法规", null))).isNotNull();
    }

    @Test
    void effectLevelFilterMiss() {
        assertThat(service.evaluate(rule(null, "{\"effectLevel\":[\"行政法规\"]}"),
                doc("某法律", "法律", null))).isNull();
    }

    @Test
    void effectLevelCodeExpandedToChinese() {
        // UI 传 code「regulation」，库里 legalLevel 是中文「行政法规」，应经 EffectLevelMapping 展开后命中
        assertThat(service.evaluate(rule(null, "{\"effectLevel\":[\"regulation\"]}"),
                doc("某条例", "行政法规", null))).isNotNull();
    }

    @Test
    void effectLevelCodeMiss() {
        // code「law」展开为 宪法/法律/司法解释，不含「行政法规」→ 不命中
        assertThat(service.evaluate(rule(null, "{\"effectLevel\":[\"law\"]}"),
                doc("某条例", "行政法规", null))).isNull();
    }

    @Test
    void regionCodePrefixHit() {
        // 省级规则 1100 前缀匹配市/区级法规 110000
        assertThat(service.evaluate(rule(null, "{\"regionCode\":[\"1100\"]}"),
                doc("北京市某规定", "地方政府规章", "110000"))).isNotNull();
    }

    @Test
    void regionCodeMiss() {
        assertThat(service.evaluate(rule(null, "{\"regionCode\":[\"3100\"]}"),
                doc("北京市某规定", "地方政府规章", "110000"))).isNull();
    }

    @Test
    void noConditionMatchesAll() {
        assertThat(service.evaluate(rule(null, null), doc("任意法规", "法律", null))).isNotNull();
    }

    @Test
    void multiDimAndSemantics() {
        // 关键词命中但效力级别不匹配 → 整体不命中（维度间 AND）
        assertThat(service.evaluate(rule("数据", "{\"effectLevel\":[\"法律\"]}"),
                doc("数据安全管理条例", "行政法规", null))).isNull();
    }
}
