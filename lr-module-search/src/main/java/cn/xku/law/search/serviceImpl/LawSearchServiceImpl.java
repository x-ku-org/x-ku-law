package cn.xku.law.search.serviceImpl;

import cn.xku.law.common.client.SearchClient;
import cn.xku.law.common.client.dto.GroupDateBucket;
import cn.xku.law.common.client.dto.SearchAggSpec;
import cn.xku.law.common.client.dto.SearchHit;
import cn.xku.law.common.client.dto.SearchPage;
import cn.xku.law.common.client.dto.SearchQuery;
import cn.xku.law.common.constant.EffectLevelMapping;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.mapper.LawVersionMapper;
import cn.xku.law.search.domain.SearchHistoryDO;
import cn.xku.law.search.domain.dto.LawSearchQueryDTO;
import cn.xku.law.search.domain.vo.LawSearchPageVO;
import cn.xku.law.search.domain.vo.LawSearchResultVO;
import cn.xku.law.search.domain.vo.MatrixBucketVO;
import cn.xku.law.law.mapper.SearchIndexTaskMapper;
import cn.xku.law.search.mapper.SearchHistoryMapper;
import cn.xku.law.search.service.LawSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.xku.law.law.domain.SearchIndexTaskDO;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawSearchServiceImpl implements LawSearchService {

    private static final String INDEX_NAME = "law_document";

    private final SearchClient searchClient;
    private final LawVersionMapper lawVersionMapper;
    private final LawDocumentMapper lawDocumentMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchIndexTaskMapper searchIndexTaskMapper;

    @Override
    public LawSearchPageVO search(LawSearchQueryDTO query) {
        Map<String, Object> filters = buildVisibilityFilters();

        if (StringUtils.hasText(query.getEffectLevel())) {
            // 前端传 code，ES 存中文原值；展开为中文原值集合走 terms 多值过滤
            filters.put("effectLevel", EffectLevelMapping.toRawValues(query.getEffectLevel()));
        }
        if (StringUtils.hasText(query.getStatus())) {
            filters.put("status", query.getStatus());
        }
        if (StringUtils.hasText(query.getPublishAuthority())) {
            filters.put("publishAuthority", query.getPublishAuthority());
        }
        if (StringUtils.hasText(query.getRegionCode())) {
            filters.put("regionCode", query.getRegionCode());
        }

        int from = (query.getPageNo() - 1) * query.getPageSize();
        SearchQuery searchQuery = new SearchQuery(INDEX_NAME, query.getKeyword(), filters, from, query.getPageSize(), null);
        applySort(searchQuery, query.getSort());
        searchQuery.setAggSpec(new SearchAggSpec("documentId", "effectLevel", "effectiveDate"));

        SearchPage page = searchClient.search(searchQuery);

        int hitCount = page.getHits().size();
        List<LawSearchResultVO> vos = assembleResults(page.getHits());
        // total 保持 ES 对全结果集的估计；filteredCount 如实反映本页因回查 MySQL 被剔脏的条数
        int filteredCount = hitCount - vos.size();

        recordHistory(query.getKeyword(), (int) page.getTotal());

        LawSearchPageVO result = new LawSearchPageVO();
        result.setTotal(page.getTotal());
        result.setList(vos);
        result.setFilteredCount(filteredCount);
        result.setDocumentCount(page.getDistinctCount());
        result.setMatrix(toMatrix(page.getGroupDateBuckets()));
        return result;
    }

    /**
     * 把前端排序意图翻译为底层检索排序字段。
     * relevance（或留空）→ 不指定排序，由检索层按「有关键词→相关度 / 无关键词→生效日期倒序」兜底；
     * time_desc / time_asc → 按生效日期降序 / 升序。
     */
    private void applySort(SearchQuery searchQuery, String sort) {
        if (!StringUtils.hasText(sort) || "relevance".equals(sort)) {
            return;
        }
        switch (sort) {
            case "time_desc" -> {
                searchQuery.setSortField("effectiveDate");
                searchQuery.setSortOrder("desc");
            }
            case "time_asc" -> {
                searchQuery.setSortField("effectiveDate");
                searchQuery.setSortOrder("asc");
            }
            default -> log.warn("[Search] unknown sort '{}', falling back to default", sort);
        }
    }

    /** 把通用聚合桶映射为前端矩阵桶（按 effectLevel × year 聚合，作用于全结果集） */
    private List<MatrixBucketVO> toMatrix(List<GroupDateBucket> buckets) {
        if (buckets == null || buckets.isEmpty()) {
            return List.of();
        }
        return buckets.stream()
                .map(b -> new MatrixBucketVO(b.getGroup(), b.getYear(), b.getCount()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildVisibilityFilters() {
        Map<String, Object> filters = new HashMap<>();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        if (tenantId == null || tenantId == 0L) {
            // 公共租户兜底：只看公开法规（检索已要求登录，无租户上下文时退化为公开范围）
            filters.put("tenantId", 0L);
        } else {
            filters.put("tenantId", List.of(0L, tenantId));
        }
        return filters;
    }

    private List<LawSearchResultVO> assembleResults(List<SearchHit> hits) {
        if (hits.isEmpty()) return List.of();

        List<Long> versionIds = hits.stream()
                .map(h -> Long.parseLong(h.getId()))
                .collect(Collectors.toList());

        List<LawVersionDO> versions = lawVersionMapper.selectBatchIds(versionIds);
        Map<Long, LawVersionDO> versionMap = versions.stream()
                .collect(Collectors.toMap(LawVersionDO::getId, v -> v));

        List<Long> documentIds = versions.stream()
                .map(LawVersionDO::getDocumentId)
                .distinct().collect(Collectors.toList());
        Map<Long, LawDocumentDO> docMap = lawDocumentMapper.selectBatchIds(documentIds).stream()
                .collect(Collectors.toMap(LawDocumentDO::getId, d -> d));

        // 保持 ES 相关度排序
        Map<String, SearchHit> hitById = hits.stream()
                .collect(Collectors.toMap(SearchHit::getId, h -> h));

        List<Long> dirtyVersionIds = new ArrayList<>();
        List<LawSearchResultVO> results = versionIds.stream().map(vid -> {
            LawVersionDO v = versionMap.get(vid);
            if (v == null) {
                dirtyVersionIds.add(vid);
                return null;
            }
            LawDocumentDO d = docMap.get(v.getDocumentId());
            boolean isPublished = "published".equals(v.getVersionStatus());
            boolean isCurrent = d != null && vid.equals(d.getCurrentVersionId());
            if (!isPublished || !isCurrent) {
                dirtyVersionIds.add(vid);
                return null;
            }
            LawSearchResultVO vo = new LawSearchResultVO();
            vo.setVersionId(vid);
            vo.setDocumentId(v.getDocumentId());
            vo.setTitle(d.getTitle());
            vo.setDocNumber(d.getDocumentNo());
            vo.setEffectLevel(d.getLegalLevel());
            vo.setStatus(d.getStatus());
            vo.setPublishAuthority(d.getIssuingOrg());
            vo.setEffectiveDate(v.getEffectiveDate());
            SearchHit hit = hitById.get(String.valueOf(vid));
            vo.setHighlights(hit != null ? hit.getHighlights() : null);
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (!dirtyVersionIds.isEmpty()) {
            log.warn("[Search] {} dirty ES hits filtered out (not published/current), scheduling compensation tasks: {}",
                    dirtyVersionIds.size(), dirtyVersionIds);
            createCompensationDeleteTasks(dirtyVersionIds);
        }
        return results;
    }

    private void createCompensationDeleteTasks(List<Long> versionIds) {
        for (Long versionId : versionIds) {
            try {
                if (searchIndexTaskMapper.countActiveDeleteTask("law_version", versionId) > 0) {
                    continue;
                }
                SearchIndexTaskDO task = new SearchIndexTaskDO();
                task.setRefType("law_version");
                task.setRefId(versionId);
                task.setIndexName("law_document");
                task.setActionType("delete");
                task.setSyncStatus("pending");
                task.setRetryCount(0);
                searchIndexTaskMapper.insert(task);
            } catch (Exception e) {
                log.warn("[Search] failed to create compensation delete task for versionId={}: {}", versionId, e.getMessage());
            }
        }
    }

    private void recordHistory(String keyword, int resultCount) {
        try {
            SearchHistoryDO history = new SearchHistoryDO();
            history.setUserId(SecurityUtils.getCurrentUserId());
            history.setKeyword(keyword);
            history.setSearchType("keyword");
            history.setResultCount(resultCount);
            history.setSearchTime(LocalDateTime.now());
            searchHistoryMapper.insert(history);
        } catch (Exception e) {
            log.warn("[Search] failed to record history: {}", e.getMessage());
        }
    }
}
