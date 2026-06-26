package cn.xku.law.law.diff;

import cn.xku.law.law.domain.LawArticleDO;
import cn.xku.law.law.mapper.LawArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 两个法规版本之间的逐条对比算法。纯计算、无副作用：读取两版本条款，按条款号对齐，
 * 逐条判定 新增/删除/修改/未变。供 {@code ChangeAnalysisStage}（管线自动对比）与
 * {@code LawCompareController}（按需对比）复用。
 *
 * <p>对齐键：优先用 {@code articleNo}；无条款号的退化「全文条款」用稳定占位键
 * {@code __order_{articleOrder}} 兜底，避免不同条款挤进同一空键。
 */
@Service
@RequiredArgsConstructor
public class ArticleDiffService {

    private final LawArticleMapper articleMapper;

    /** 计算 base→target 的逐条差异。 */
    public VersionDiffResult diff(Long baseVersionId, Long targetVersionId) {
        Map<String, LawArticleDO> baseMap = loadAligned(baseVersionId);
        Map<String, LawArticleDO> targetMap = loadAligned(targetVersionId);

        // 以「基准顺序 + 目标新增顺序」组织输出，保证明细顺序稳定可读。
        List<String> orderedKeys = new ArrayList<>(baseMap.keySet());
        for (String k : targetMap.keySet()) {
            if (!baseMap.containsKey(k)) {
                orderedKeys.add(k);
            }
        }

        List<ArticleChange> changes = new ArrayList<>(orderedKeys.size());
        int added = 0, removed = 0, modified = 0, unchanged = 0;
        for (String key : orderedKeys) {
            LawArticleDO base = baseMap.get(key);
            LawArticleDO target = targetMap.get(key);
            ArticleChange change = new ArticleChange();
            change.setArticleNo(target != null ? target.getArticleNo() : base.getArticleNo());
            change.setArticleTitle(target != null ? target.getArticleTitle()
                    : (base != null ? base.getArticleTitle() : null));

            if (base == null) {
                change.setChangeType(ArticleChangeType.ADDED);
                change.setTargetText(target.getContentText());
                added++;
            } else if (target == null) {
                change.setChangeType(ArticleChangeType.REMOVED);
                change.setBaseText(base.getContentText());
                removed++;
            } else if (!contentEquals(base.getContentText(), target.getContentText())) {
                change.setChangeType(ArticleChangeType.MODIFIED);
                change.setBaseText(base.getContentText());
                change.setTargetText(target.getContentText());
                modified++;
            } else {
                change.setChangeType(ArticleChangeType.UNCHANGED);
                change.setBaseText(base.getContentText());
                change.setTargetText(target.getContentText());
                unchanged++;
            }
            changes.add(change);
        }

        VersionDiffResult result = new VersionDiffResult();
        result.setBaseVersionId(baseVersionId);
        result.setTargetVersionId(targetVersionId);
        result.setAddedCount(added);
        result.setRemovedCount(removed);
        result.setModifiedCount(modified);
        result.setUnchangedCount(unchanged);
        result.setChangeCount(added + removed + modified);
        result.setSummary(buildSummary(added, modified, removed));
        result.setChanges(changes);
        return result;
    }

    /** 读取某版本全部条款并按对齐键建表；重复键以先到者为准（按 article_order 升序）。 */
    private Map<String, LawArticleDO> loadAligned(Long versionId) {
        List<LawArticleDO> articles = articleMapper.selectList(
                new LambdaQueryWrapper<LawArticleDO>()
                        .eq(LawArticleDO::getVersionId, versionId)
                        .orderByAsc(LawArticleDO::getArticleOrder));
        Map<String, LawArticleDO> map = new LinkedHashMap<>();
        for (LawArticleDO a : articles) {
            map.putIfAbsent(alignKey(a), a);
        }
        return map;
    }

    private static String alignKey(LawArticleDO a) {
        if (StringUtils.hasText(a.getArticleNo())) {
            return a.getArticleNo().trim();
        }
        return "__order_" + a.getArticleOrder();
    }

    private static boolean contentEquals(String a, String b) {
        String na = a == null ? "" : a.trim();
        String nb = b == null ? "" : b.trim();
        return na.equals(nb);
    }

    private static String buildSummary(int added, int modified, int removed) {
        if (added == 0 && modified == 0 && removed == 0) {
            return "与上一版本无条款差异";
        }
        StringBuilder sb = new StringBuilder();
        if (added > 0) sb.append("新增").append(added).append("条、");
        if (modified > 0) sb.append("修改").append(modified).append("条、");
        if (removed > 0) sb.append("删除").append(removed).append("条、");
        sb.setLength(sb.length() - 1); // 去掉尾部「、」
        return sb.toString();
    }
}
