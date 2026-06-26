package cn.xku.law.search;

import cn.xku.law.common.client.SearchClient;
import cn.xku.law.common.client.dto.GroupDateBucket;
import cn.xku.law.common.client.dto.SearchAggSpec;
import cn.xku.law.common.client.dto.SearchHit;
import cn.xku.law.common.client.dto.SearchPage;
import cn.xku.law.common.client.dto.SearchQuery;
import cn.xku.law.common.client.dto.VectorMatch;
import cn.xku.law.common.exception.SearchUnavailableException;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("elasticsearchSearchClient")
@ConditionalOnProperty(prefix = "app.search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ElasticsearchSearchClient implements SearchClient {

    private static final String LAW_INDEX = "law_document";
    private static final String[] HIGHLIGHT_FIELDS = {"title", "contentText"};
    private static final String VECTOR_FIELD = "embedding";

    private final ElasticsearchClient esClient;

    @Value("${app.vector.enabled:false}")
    private boolean vectorEnabled;

    @Value("${app.vector.index-name:law_segment}")
    private String vectorIndex;

    @Value("${app.vector.dimension:1536}")
    private int vectorDimension;

    @Value("${app.vector.similarity:cosine}")
    private String vectorSimilarity;

    @PostConstruct
    public void ensureIndex() {
        ensureLawIndex();
        if (vectorEnabled) {
            ensureVectorIndex();
        }
    }

    private void ensureLawIndex() {
        try {
            BooleanResponse exists = esClient.indices().exists(
                    ExistsRequest.of(e -> e.index(LAW_INDEX)));
            if (!exists.value()) {
                CreateIndexResponse resp = esClient.indices().create(c -> c
                        .index(LAW_INDEX)
                        .mappings(m -> m
                                .properties("versionId",  p -> p.long_(l -> l))
                                .properties("documentId", p -> p.long_(l -> l))
                                .properties("title",      p -> p.text(t -> t.analyzer("standard")))
                                .properties("docNumber",  p -> p.keyword(k -> k))
                                .properties("contentText",p -> p.text(t -> t.analyzer("standard")))
                                .properties("effectLevel",p -> p.keyword(k -> k))
                                .properties("status",     p -> p.keyword(k -> k))
                                .properties("publishAuthority", p -> p.keyword(k -> k))
                                .properties("regionCode", p -> p.keyword(k -> k))
                                .properties("effectiveDate", p -> p.date(d -> d))
                                .properties("tenantId",   p -> p.long_(l -> l))
                                .properties("isPublic",   p -> p.boolean_(b -> b))
                        )
                );
                log.info("[ES] Index '{}' created: {}", LAW_INDEX, resp.acknowledged());
            } else {
                // 否则首次写入会被动态映射成 analyzed text（中文按字切分），地区 term 过滤将永远不命中。
                // 该字段尚不存在，putMapping 可安全新增；幂等，重复调用无副作用。
                ensureKeywordField(LAW_INDEX, "regionCode");
            }
        } catch (IOException e) {
            log.error("[ES] Failed to ensure index '{}', will retry later: {}", LAW_INDEX, e.getMessage());
        }
    }

    /** 为既有索引补一个 keyword 字段映射（幂等）；字段已存在且类型一致则 ES 视为无操作。 */
    private void ensureKeywordField(String index, String field) {
        try {
            esClient.indices().putMapping(pm -> pm
                    .index(index)
                    .properties(field, p -> p.keyword(k -> k)));
            log.info("[ES] ensured keyword field '{}.{}'", index, field);
        } catch (IOException | RuntimeException e) {
            log.warn("[ES] failed to ensure keyword field '{}.{}' (may already exist with a different type): {}",
                    index, field, e.getMessage());
        }
    }

    /** 条款分片向量索引：embedding 为 dense_vector，支持 kNN 语义检索。 */
    private void ensureVectorIndex() {
        try {
            BooleanResponse exists = esClient.indices().exists(
                    ExistsRequest.of(e -> e.index(vectorIndex)));
            if (!exists.value()) {
                CreateIndexResponse resp = esClient.indices().create(c -> c
                        .index(vectorIndex)
                        .mappings(m -> m
                                .properties("segmentId",  p -> p.long_(l -> l))
                                .properties("articleId",  p -> p.long_(l -> l))
                                .properties("versionId",  p -> p.long_(l -> l))
                                .properties("documentId", p -> p.long_(l -> l))
                                .properties("segmentText",p -> p.text(t -> t.analyzer("standard")))
                                .properties("tenantId",   p -> p.long_(l -> l))
                                .properties("isPublic",   p -> p.boolean_(b -> b))
                                .properties(VECTOR_FIELD, p -> p.denseVector(dv -> dv
                                        .dims(vectorDimension)
                                        .index(true)
                                        .similarity(vectorSimilarity)))
                        )
                );
                log.info("[ES] Vector index '{}' created (dims={}, similarity={}): {}",
                        vectorIndex, vectorDimension, vectorSimilarity, resp.acknowledged());
            }
        } catch (IOException e) {
            log.error("[ES] Failed to ensure vector index '{}', will retry later: {}", vectorIndex, e.getMessage());
        }
    }

    @Override
    public SearchPage search(SearchQuery query) {
        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                // 短语匹配：标准分析器把中文切成单字，phrase 要求关键词各字相邻出现，
                // title 权重更高，使标题命中排在正文命中之前。
                boolBuilder.must(Query.of(q -> q.multiMatch(mm -> mm
                        .query(query.getKeyword())
                        .type(TextQueryType.Phrase)
                        .fields("title^3", "contentText"))));
            } else {
                boolBuilder.must(Query.of(q -> q.matchAll(ma -> ma)));
            }

            if (query.getFilters() != null) {
                query.getFilters().forEach((field, value) -> {
                    if (value == null) return;
                    if (value instanceof java.util.Collection<?> col) {
                        // 多值用 terms 查询
                        List<FieldValue> fvs = col.stream()
                                .map(v -> FieldValue.of(v.toString()))
                                .collect(Collectors.toList());
                        boolBuilder.filter(f -> f.terms(t -> t.field(field)
                                .terms(tv -> tv.value(fvs))));
                    } else {
                        boolBuilder.filter(f -> f.term(t -> t.field(field)
                                .value(FieldValue.of(value.toString()))));
                    }
                });
            }

            String[] hlFields = query.getHighlightFields() != null
                    ? query.getHighlightFields() : HIGHLIGHT_FIELDS;

            SearchRequest.Builder reqBuilder = new SearchRequest.Builder()
                    .index(query.getIndex())
                    .from(query.getFrom())
                    .size(query.getSize())
                    .trackTotalHits(t -> t.count(50000))
                    .query(Query.of(q -> q.bool(boolBuilder.build())))
                    .highlight(h -> {
                        h.preTags("<em>").postTags("</em>");
                        for (String field : hlFields) {
                            h.fields(field, hf -> hf);
                        }
                        return h;
                    })
                    .source(s -> s.fetch(false));

            applySort(reqBuilder, query);

            applyAggregations(reqBuilder, query.getAggSpec());

            SearchResponse<Map> response = esClient.search(reqBuilder.build(), Map.class);
            HitsMetadata<Map> hits = response.hits();
            long total = hits.total() != null ? hits.total().value() : 0L;

            List<SearchHit> resultHits = hits.hits().stream().map(h -> {
                Map<String, List<String>> highlights = new HashMap<>();
                if (h.highlight() != null) {
                    h.highlight().forEach((k, v) -> highlights.put(k, new ArrayList<>(v)));
                }
                return new SearchHit(h.id(), highlights);
            }).collect(Collectors.toList());

            SearchPage searchPage = new SearchPage(total, resultHits);
            parseAggregations(response, query.getAggSpec(), searchPage);
            return searchPage;
        } catch (IOException e) {
            log.error("[ES] search failed: index={}, keyword={}", query.getIndex(), query.getKeyword(), e);
            throw new SearchUnavailableException("检索服务不可用，请稍后重试", e);
        }
    }

    /**
     * 应用排序：
     *  - 指定 sortField 时按该字段排序，并以 _score 兜底（同值时保留相关度次序）；missing 值排在末尾；
     *  - 未指定且有关键词时，沿用 ES 默认的 _score 倒序（相关度）；
     *  - 未指定且无关键词时，matchAll 评分全为 1 会退化为不确定的内部序，
     *    故显式按 effectiveDate 倒序（最新在前）使浏览结果稳定可预期。
     */
    private void applySort(SearchRequest.Builder reqBuilder, SearchQuery query) {
        String sortField = query.getSortField();
        if (sortField != null && !sortField.isBlank()) {
            SortOrder order = "asc".equalsIgnoreCase(query.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc;
            reqBuilder.sort(so -> so.field(f -> f.field(sortField).order(order).missing("_last")));
            reqBuilder.sort(so -> so.score(sc -> sc.order(SortOrder.Desc)));
            return;
        }
        boolean hasKeyword = query.getKeyword() != null && !query.getKeyword().isBlank();
        if (!hasKeyword) {
            reqBuilder.sort(so -> so.field(f -> f.field("effectiveDate").order(SortOrder.Desc).missing("_last")));
        }
    }

    /** 去重计数聚合名 */
    private static final String AGG_DISTINCT = "distinct_count";
    /** 分组聚合名（terms） */
    private static final String AGG_GROUP = "group";
    /** 年份子聚合名（date_histogram） */
    private static final String AGG_YEAR = "year";

    /** 按聚合规格挂载 cardinality（去重计数）与 terms×date_histogram（分组×年份）聚合 */
    private void applyAggregations(SearchRequest.Builder reqBuilder, SearchAggSpec spec) {
        if (spec == null) {
            return;
        }
        if (spec.getDistinctField() != null && !spec.getDistinctField().isBlank()) {
            reqBuilder.aggregations(AGG_DISTINCT, a -> a
                    .cardinality(c -> c.field(spec.getDistinctField())));
        }
        if (spec.getGroupField() != null && !spec.getGroupField().isBlank()
                && spec.getDateField() != null && !spec.getDateField().isBlank()) {
            reqBuilder.aggregations(AGG_GROUP, a -> a
                    .terms(t -> t.field(spec.getGroupField()).size(50))
                    .aggregations(AGG_YEAR, sub -> sub
                            .dateHistogram(dh -> dh
                                    .field(spec.getDateField())
                                    .calendarInterval(CalendarInterval.Year)
                                    .format("yyyy")
                                    .minDocCount(1))));
        }
    }

    /** 解析聚合结果写入 SearchPage；ES 未返回对应聚合时保持默认值（0 / 空列表） */
    private void parseAggregations(SearchResponse<Map> response, SearchAggSpec spec, SearchPage page) {
        if (spec == null || response.aggregations() == null || response.aggregations().isEmpty()) {
            return;
        }
        Aggregate distinct = response.aggregations().get(AGG_DISTINCT);
        if (distinct != null && distinct.isCardinality()) {
            page.setDistinctCount(distinct.cardinality().value());
        }
        Aggregate group = response.aggregations().get(AGG_GROUP);
        if (group != null && group.isSterms()) {
            List<GroupDateBucket> buckets = new ArrayList<>();
            group.sterms().buckets().array().forEach(gb -> {
                String groupKey = gb.key().stringValue();
                Aggregate yearAgg = gb.aggregations().get(AGG_YEAR);
                if (yearAgg != null && yearAgg.isDateHistogram()) {
                    yearAgg.dateHistogram().buckets().array().forEach(yb -> {
                        int year = parseYear(yb.keyAsString());
                        if (year > 0) {
                            buckets.add(new GroupDateBucket(groupKey, year, yb.docCount()));
                        }
                    });
                }
            });
            page.setGroupDateBuckets(buckets);
        }
    }

    /** 从 date_histogram 的 keyAsString（format=yyyy）解析年份，无法解析时返回 0 */
    private static int parseYear(String value) {
        if (value == null || value.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public List<String> search(String index, String keyword, Map<String, Object> filters, int from, int size) {
        SearchQuery q = new SearchQuery(index, keyword, filters, from, size, null);
        return search(q).getHits().stream().map(SearchHit::getId).collect(Collectors.toList());
    }

    @Override
    public void indexDocument(String index, String docId, Map<String, Object> source) {
        try {
            esClient.index(r -> r.index(index).id(docId).document(source));
            log.debug("[ES] indexed {}/{}", index, docId);
        } catch (IOException e) {
            log.error("[ES] indexDocument failed: {}/{}", index, docId, e);
            throw new SearchUnavailableException("索引写入失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDocument(String index, String docId) {
        try {
            esClient.delete(r -> r.index(index).id(docId));
            log.debug("[ES] deleted {}/{}", index, docId);
        } catch (IOException e) {
            log.error("[ES] deleteDocument failed: {}/{}", index, docId, e);
            throw new SearchUnavailableException("索引删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> vectorSearch(String index, float[] vector, int k) {
        return vectorSearchScored(index, vector, k).stream()
                .map(VectorMatch::id)
                .collect(Collectors.toList());
    }

    @Override
    public List<VectorMatch> vectorSearchScored(String index, float[] vector, int k) {
        if (vector == null || vector.length == 0) {
            return Collections.emptyList();
        }
        List<Float> queryVector = new ArrayList<>(vector.length);
        for (float v : vector) {
            queryVector.add(v);
        }
        int numCandidates = Math.max(k * 10, 100);
        try {
            SearchResponse<Map> response = esClient.search(s -> s
                    .index(index)
                    .knn(kn -> kn
                            .field(VECTOR_FIELD)
                            .queryVector(queryVector)
                            .k(k)
                            .numCandidates(numCandidates))
                    .source(src -> src.fetch(false)), Map.class);

            // ES 已按 _score 降序返回；cosine 相似度下 _score ∈ [0,1]。
            return response.hits().hits().stream()
                    .map(h -> new VectorMatch(h.id(), h.score() != null ? h.score() : 0d))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("[ES] vectorSearch failed: index={}, k={}", index, k, e);
            throw new SearchUnavailableException("向量检索服务不可用，请稍后重试", e);
        }
    }
}
