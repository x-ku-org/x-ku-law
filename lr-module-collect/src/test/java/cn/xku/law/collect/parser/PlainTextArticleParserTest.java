package cn.xku.law.collect.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlainTextArticleParserTest {

    private final PlainTextArticleParser parser = new PlainTextArticleParser();

    @Test
    void splitsArticlesAndIgnoresReferences() {
        String text = """
                第一章 总则
                第一条 为了规范市场秩序，保护当事人合法权益，制定本法。
                第二条 本法第二十一条规定的情形，应当依法处理；并参照第十条确定责任。
                第四条 主管部门应当建立监督检查制度。
                第五条 这是一个被截断的尾部条款
                """;

        ParseResult result = parser.parse(ParseInput.ofText(text, Map.of()));

        assertThat(result.getParseStatus()).isEqualTo("parsed");
        assertThat(result.getArticles())
                .extracting(ParsedArticle::getArticleNo)
                .containsExactly("第一条", "第二条", "第四条");
        assertThat(result.getArticles().get(1).getContentText())
                .contains("本法第二十一条")
                .contains("参照第十条");
    }

    @Test
    void mergesMidArticleCrossReferenceBackIntoEnclosingArticle() {
        // 第七十条正文里夹了一句对「第五十三条」的引用，前缀「符合」不在白名单，旧逻辑会误切成新条。
        String text = """
                第六十九条 主管部门应当依法履行监督管理职责，建立健全监督检查制度。
                第七十条 当事人不符合第五十三条规定的条件的，主管部门不予批准并书面说明理由。
                第七十一条 本办法自公布之日起施行。
                """;

        ParseResult result = parser.parse(ParseInput.ofText(text, Map.of()));

        // 不应出现独立的第二个「第五十三条」。
        assertThat(result.getArticles())
                .extracting(ParsedArticle::getArticleNo)
                .containsExactly("第六十九条", "第七十条", "第七十一条");
        // 被误切的引用文本应并回第七十条正文。
        ParsedArticle art70 = result.getArticles().stream()
                .filter(a -> "第七十条".equals(a.getArticleNo()))
                .findFirst().orElseThrow();
        assertThat(art70.getContentText()).contains("不符合第五十三条规定的条件");
    }

    @Test
    void dropsExactDuplicateArticleNo() {
        // 同一条号出现两次（如目录+正文），只应保留一条。
        String text = """
                第五十二条 经营者应当依法办理登记手续，取得相应资质。
                第五十三条 经营者应当如实记录交易信息并妥善保存备查。
                第五十三条 经营者应当如实记录交易信息并妥善保存备查。
                第五十四条 主管部门应当对经营活动进行监督检查处理。
                """;

        ParseResult result = parser.parse(ParseInput.ofText(text, Map.of()));

        assertThat(result.getArticles())
                .extracting(ParsedArticle::getArticleNo)
                .containsExactly("第五十二条", "第五十三条", "第五十四条");
    }

    @Test
    void keepsSubArticleInOrder() {
        // 「第十条之一」是子条款，排在第十条与第十一条之间，三者都应保留且顺序正确。
        String text = """
                第十条 申请人应当提交符合要求的全部申请材料并签字确认。
                第十条之一 申请材料不齐全的，主管部门应当一次性告知需要补正的内容。
                第十一条 主管部门应当在法定期限内作出是否准予的决定并送达。
                """;

        ParseResult result = parser.parse(ParseInput.ofText(text, Map.of()));

        assertThat(result.getArticles())
                .extracting(ParsedArticle::getArticleNo)
                .containsExactly("第十条", "第十条之一", "第十一条");
    }

    @Test
    void stripsTrailingChapterTitleFromPreviousArticle() {
        String text = "第一条 本办法自公布之日起施行。 第二章 监督管理\n"
                + "第二条 主管部门应当依法履行监督管理职责。";

        ParseResult result = parser.parse(ParseInput.ofText(text, Map.of()));

        assertThat(result.getArticles()).hasSize(2);
        assertThat(result.getArticles().get(0).getContentText())
                .isEqualTo("第一条 本办法自公布之日起施行。");
    }
}
