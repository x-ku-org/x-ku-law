package cn.xku.law.law.diff;

import cn.xku.law.law.domain.LawArticleDO;
import cn.xku.law.law.mapper.LawArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** ArticleDiffService 单测：覆盖 新增/删除/修改/未变 四类对齐（Mockito，无 Spring）。 */
class ArticleDiffServiceTest {

    private LawArticleMapper articleMapper;
    private ArticleDiffService service;

    @BeforeEach
    void setUp() {
        articleMapper = mock(LawArticleMapper.class);
        service = new ArticleDiffService(articleMapper);
    }

    private LawArticleDO article(String articleNo, int order, String content) {
        LawArticleDO a = new LawArticleDO();
        a.setArticleNo(articleNo);
        a.setArticleOrder(order);
        a.setContentText(content);
        return a;
    }

    /**
     * 路由 base/target：diff() 内部先 loadAligned(base) 再 loadAligned(target)，
     * 调用顺序稳定，用顺序桩返回即可（不内省 wrapper，避免触发 MyBatis lambda 缓存）。
     */
    private void stub(List<LawArticleDO> base, List<LawArticleDO> target) {
        when(articleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(base, target);
    }

    @Test
    void diff_classifiesAddedRemovedModifiedUnchanged() {
        List<LawArticleDO> base = List.of(
                article("第一条", 1, "甲"),      // unchanged
                article("第二条", 2, "乙旧"),     // modified
                article("第三条", 3, "丙"));      // removed
        List<LawArticleDO> target = List.of(
                article("第一条", 1, "甲"),       // unchanged
                article("第二条", 2, "乙新"),      // modified
                article("第四条", 4, "丁"));       // added

        stub(base, target);
        VersionDiffResult r = service.diff(1L, 2L);

        assertThat(r.getAddedCount()).isEqualTo(1);
        assertThat(r.getRemovedCount()).isEqualTo(1);
        assertThat(r.getModifiedCount()).isEqualTo(1);
        assertThat(r.getUnchangedCount()).isEqualTo(1);
        assertThat(r.getChangeCount()).isEqualTo(3);
        assertThat(r.getSummary()).isEqualTo("新增1条、修改1条、删除1条");

        Map<String, ArticleChange> byNo = r.getChanges().stream()
                .collect(java.util.stream.Collectors.toMap(ArticleChange::getArticleNo, c -> c));
        assertThat(byNo.get("第一条").getChangeType()).isEqualTo(ArticleChangeType.UNCHANGED);
        assertThat(byNo.get("第二条").getChangeType()).isEqualTo(ArticleChangeType.MODIFIED);
        assertThat(byNo.get("第二条").getBaseText()).isEqualTo("乙旧");
        assertThat(byNo.get("第二条").getTargetText()).isEqualTo("乙新");
        assertThat(byNo.get("第三条").getChangeType()).isEqualTo(ArticleChangeType.REMOVED);
        assertThat(byNo.get("第三条").getTargetText()).isNull();
        assertThat(byNo.get("第四条").getChangeType()).isEqualTo(ArticleChangeType.ADDED);
        assertThat(byNo.get("第四条").getBaseText()).isNull();
    }

    @Test
    void diff_noChange_givesEmptySummary() {
        List<LawArticleDO> same = List.of(article("第一条", 1, "内容"));
        stub(same, same);
        VersionDiffResult r = service.diff(1L, 2L);
        assertThat(r.getChangeCount()).isZero();
        assertThat(r.getSummary()).isEqualTo("与上一版本无条款差异");
    }

    @Test
    void diff_whitespaceOnlyChange_isUnchanged() {
        List<LawArticleDO> base = List.of(article("第一条", 1, "  内容 "));
        List<LawArticleDO> target = List.of(article("第一条", 1, "内容"));
        stub(base, target);
        VersionDiffResult r = service.diff(1L, 2L);
        assertThat(r.getUnchangedCount()).isEqualTo(1);
        assertThat(r.getChangeCount()).isZero();
    }
}
