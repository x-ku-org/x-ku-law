package cn.xku.law.process.stage;

import cn.xku.law.law.diff.ArticleDiffService;
import cn.xku.law.law.diff.VersionDiffResult;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.service.CompareRecordService;
import cn.xku.law.law.service.LawVersionService;
import cn.xku.law.process.LawProcessingContext;
import cn.xku.law.process.LawProcessingStage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 阶段 50：版本间变更分析（逐条对比）。在当前版本发布后，找到同法规的「上一已发布版本」，
 * 调 {@link ArticleDiffService} 做逐条 diff，把摘要写回 {@code lr_law_version.diff_summary}，
 * 并以系统身份（user_id=0）落库一条 {@code lr_compare_record}（base=上一版, target=当前版）。
 *
 * <p>「上一版」：同 documentId、已发布、且 publishDate 早于当前版本，按 publishDate 倒序取第一条。
 * 首版（无上一版）直接跳过。落库幂等由 {@link CompareRecordService#saveSystemDiff} 内部清旧再插保证。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeAnalysisStage implements LawProcessingStage {

    private final LawVersionService lawVersionService;
    private final ArticleDiffService articleDiffService;
    private final CompareRecordService compareRecordService;

    @Override
    public String name() {
        return "change-analysis";
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public void process(LawProcessingContext ctx) {
        LawVersionDO current = lawVersionService.getById(ctx.getVersionId());
        if (current == null) {
            log.warn("[ChangeAnalysis] versionId={} not found, skip", ctx.getVersionId());
            return;
        }
        LawVersionDO previous = findPreviousPublished(current);
        if (previous == null) {
            log.debug("[ChangeAnalysis] versionId={} has no previous published version, skip (首版无可比)",
                    ctx.getVersionId());
            return;
        }

        VersionDiffResult diff = articleDiffService.diff(previous.getId(), current.getId());
        compareRecordService.saveSystemDiff(ctx.getDocumentId(), diff);

        current.setDiffSummary(diff.getSummary());
        lawVersionService.updateById(current);

        log.info("[ChangeAnalysis] versionId={} vs prev={} -> {} (changeCount={})",
                current.getId(), previous.getId(), diff.getSummary(), diff.getChangeCount());
    }

    /**
     * 同 documentId、已发布、publishDate 早于当前版本的最近一版。
     * publishDate 为空（当前版尚未填发布日）时退化为「同文档其它已发布版本里 publishDate 最大的一条」。
     */
    private LawVersionDO findPreviousPublished(LawVersionDO current) {
        LambdaQueryWrapper<LawVersionDO> wrapper = new LambdaQueryWrapper<LawVersionDO>()
                .eq(LawVersionDO::getDocumentId, current.getDocumentId())
                .eq(LawVersionDO::getVersionStatus, "published")
                .ne(LawVersionDO::getId, current.getId())
                .isNotNull(LawVersionDO::getPublishDate)
                .orderByDesc(LawVersionDO::getPublishDate)
                .last("LIMIT 1");
        LocalDate currentPublish = current.getPublishDate();
        if (currentPublish != null) {
            wrapper.lt(LawVersionDO::getPublishDate, currentPublish);
        }
        return lawVersionService.getOne(wrapper);
    }
}
