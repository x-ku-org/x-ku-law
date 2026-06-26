package cn.xku.law.home;

import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.search.mapper.SearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 首页门户聚合实现：复用 law / search 模块的 mapper 做轻量只读聚合。
 * 各子查询互不依赖，任一失败仅降级该板块（不影响整体返回）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeOverviewServiceImpl implements HomeOverviewService {

    private static final int LATEST_LIMIT = 6;
    private static final int TRENDING_LIMIT = 6;
    private static final int TRENDING_WINDOW_DAYS = 7;
    private static final int NEW_BADGE_DAYS = 7;

    /** 无检索历史时的兜底热点词（编辑配置，保证门户不空白） */
    private static final List<String> TRENDING_FALLBACK = List.of(
            "重要数据出境申报条件",
            "关键信息基础设施义务清单",
            "个人信息标准合同备案",
            "数据分类分级要求",
            "数据安全风险评估",
            "网络安全等级保护"
    );

    private final LawDocumentMapper lawDocumentMapper;
    private final SearchHistoryMapper searchHistoryMapper;

    @Override
    public HomeOverviewVO overview() {
        HomeOverviewVO vo = new HomeOverviewVO();
        vo.setCorpusCount(safeLong(lawDocumentMapper::countEffective));
        vo.setTodayUpdateCount(safeLong(lawDocumentMapper::countUpdatedToday));
        vo.setLevelCount((int) safeLong(lawDocumentMapper::countDistinctLevel));
        vo.setRegionCount((int) safeLong(lawDocumentMapper::countDistinctRegion));

        List<LawDocumentDO> latest = safeList(() -> lawDocumentMapper.selectLatest(LATEST_LIMIT));
        vo.setLatest(latest.stream().map(this::toLatest).collect(Collectors.toList()));
        if (!latest.isEmpty()) {
            vo.setTodayHighlight(toHighlight(latest.get(0)));
        }

        vo.setTrending(resolveTrending());
        return vo;
    }

    private List<HomeOverviewVO.Trending> resolveTrending() {
        LocalDateTime since = LocalDateTime.now().minusDays(TRENDING_WINDOW_DAYS);
        List<Map<String, Object>> rows = safeList(() -> searchHistoryMapper.selectTrending(since, TRENDING_LIMIT));
        List<HomeOverviewVO.Trending> result = rows.stream()
                .map(row -> {
                    HomeOverviewVO.Trending t = new HomeOverviewVO.Trending();
                    t.setKeyword(Objects.toString(row.get("keyword"), ""));
                    Object heat = row.get("heat");
                    t.setHeat(heat instanceof Number ? ((Number) heat).intValue() : 0);
                    return t;
                })
                .filter(t -> !t.getKeyword().isBlank())
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            return TRENDING_FALLBACK.stream().map(kw -> {
                HomeOverviewVO.Trending t = new HomeOverviewVO.Trending();
                t.setKeyword(kw);
                t.setHeat(0);
                return t;
            }).collect(Collectors.toList());
        }
        return result;
    }

    private HomeOverviewVO.Latest toLatest(LawDocumentDO doc) {
        HomeOverviewVO.Latest item = new HomeOverviewVO.Latest();
        item.setDocumentId(doc.getId());
        item.setTitle(doc.getTitle());
        item.setTag(resolveTag(doc));
        item.setDate(doc.getPublishDate());
        if (doc.getPublishDate() != null && !doc.getPublishDate().isBefore(LocalDate.now().minusDays(NEW_BADGE_DAYS))) {
            item.setBadge("新增");
        }
        return item;
    }

    private HomeOverviewVO.Highlight toHighlight(LawDocumentDO doc) {
        HomeOverviewVO.Highlight h = new HomeOverviewVO.Highlight();
        h.setDocumentId(doc.getId());
        h.setTag(resolveTag(doc));
        h.setTitle(doc.getTitle());
        h.setSummary(doc.getSummary());
        h.setDate(doc.getPublishDate());
        return h;
    }

    /** 类型标签优先取效力级别（中文），缺省回退法规类型 */
    private String resolveTag(LawDocumentDO doc) {
        if (doc.getLegalLevel() != null && !doc.getLegalLevel().isBlank()) {
            return doc.getLegalLevel();
        }
        return doc.getLawType();
    }

    private long safeLong(java.util.function.LongSupplier supplier) {
        try {
            return supplier.getAsLong();
        } catch (Exception e) {
            log.warn("[home-overview] 统计查询失败，降级为 0", e);
            return 0L;
        }
    }

    private <T> List<T> safeList(java.util.function.Supplier<List<T>> supplier) {
        try {
            List<T> list = supplier.get();
            return list != null ? list : List.of();
        } catch (Exception e) {
            log.warn("[home-overview] 列表查询失败，降级为空", e);
            return List.of();
        }
    }
}
