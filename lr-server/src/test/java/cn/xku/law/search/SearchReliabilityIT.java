package cn.xku.law.search;

import cn.xku.law.common.client.SearchClient;
import cn.xku.law.common.client.dto.SearchHit;
import cn.xku.law.common.client.dto.SearchPage;
import cn.xku.law.common.client.dto.SearchQuery;
import cn.xku.law.search.domain.dto.LawSearchQueryDTO;
import cn.xku.law.search.domain.vo.LawSearchPageVO;
import cn.xku.law.search.service.LawSearchService;
import cn.xku.law.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 检索可靠性集成测试（用桩 SearchClient + 真实 MySQL，无需 ES），覆盖：
 *  - ES 命中回查 MySQL，仅保留 published 且为当前版本者；
 *  - 回归 #2（P1-2）：total 保持 ES 估计、filteredCount 如实反映本页剔脏数；
 *  - 回归 #3（P1-3）：同一脏命中多次检索只生成一条在途补偿任务（去重）。
 */
@Import(SearchReliabilityIT.StubSearchConfig.class)
class SearchReliabilityIT extends AbstractIntegrationTest {

    @Autowired
    private LawSearchService lawSearchService;

    /** 桩：ES 命中 201（干净）/202（非当前版本，脏）/203（MySQL 无对应版本，脏），total=3。 */
    @Configuration
    static class StubSearchConfig {
        @Bean
        @Primary
        SearchClient stubSearchClient() {
            return new SearchClient() {
                @Override
                public SearchPage search(SearchQuery query) {
                    return new SearchPage(3L, List.of(
                            new SearchHit("201", null),
                            new SearchHit("202", null),
                            new SearchHit("203", null)));
                }

                @Override
                public List<String> search(String index, String query, Map<String, Object> filters, int from, int size) {
                    return List.of();
                }

                @Override
                public List<String> vectorSearch(String index, float[] vector, int k) {
                    return List.of();
                }

                @Override
                public void indexDocument(String index, String docId, Map<String, Object> source) {
                }

                @Override
                public void deleteDocument(String index, String docId) {
                }
            };
        }
    }

    private void seedLawData() {
        jdbcTemplate.update("INSERT INTO lr_law_document (id, law_uid, title, current_version_id, status, tenant_id) " +
                "VALUES (101, 'uid-clean', '干净法规', 201, 'published', 0)");
        jdbcTemplate.update("INSERT INTO lr_law_version (id, document_id, version_no, version_status, tenant_id) " +
                "VALUES (201, 101, 'v1', 'published', 0)");
        jdbcTemplate.update("INSERT INTO lr_law_document (id, law_uid, title, current_version_id, status, tenant_id) " +
                "VALUES (102, 'uid-dirty', '脏法规', 999, 'published', 0)");
        jdbcTemplate.update("INSERT INTO lr_law_version (id, document_id, version_no, version_status, tenant_id) " +
                "VALUES (202, 102, 'v1', 'superseded', 0)");
    }

    private int pendingDeleteCount(long versionId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lr_search_index_task WHERE ref_type='law_version' AND ref_id=? " +
                        "AND action_type='delete' AND sync_status IN ('pending','processing')",
                Integer.class, versionId);
    }

    @Test
    void dirtyHitsFilteredAndPaginationConsistent() {
        seedLawData();
        LawSearchQueryDTO query = new LawSearchQueryDTO();
        query.setKeyword("法规");

        LawSearchPageVO result = lawSearchService.search(query);

        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getVersionId()).isEqualTo(201L);
        // total 保持 ES 估计值 3；filteredCount 如实反映本页剔脏 2 条
        assertThat(result.getTotal()).isEqualTo(3L);
        assertThat(result.getFilteredCount()).isEqualTo(2);
    }

    @Test
    void compensationTasksAreDeduplicatedAcrossRepeatedSearches() {
        seedLawData();
        LawSearchQueryDTO query = new LawSearchQueryDTO();
        query.setKeyword("法规");

        lawSearchService.search(query);
        lawSearchService.search(query);
        lawSearchService.search(query);

        assertThat(pendingDeleteCount(202L)).isEqualTo(1);
        assertThat(pendingDeleteCount(203L)).isEqualTo(1);
    }
}
