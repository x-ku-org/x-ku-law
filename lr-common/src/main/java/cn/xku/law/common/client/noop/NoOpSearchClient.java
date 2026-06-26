package cn.xku.law.common.client.noop;

import cn.xku.law.common.client.SearchClient;
import cn.xku.law.common.client.dto.SearchPage;
import cn.xku.law.common.client.dto.SearchQuery;
import cn.xku.law.common.exception.SearchUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** SearchClient 空实现，ES 未接入时自动激活，防止启动失败 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "elasticsearchSearchClient")
public class NoOpSearchClient implements SearchClient {

    @Override
    public SearchPage search(SearchQuery query) {
        log.warn("[NoOpSearchClient] search called — ES not configured. index={}, keyword={}", query.getIndex(), query.getKeyword());
        throw new SearchUnavailableException("检索服务不可用，请稍后重试");
    }

    @Override
    public List<String> search(String index, String query, Map<String, Object> filters, int from, int size) {
        log.warn("[NoOpSearchClient] search called — ES not configured. index={}, query={}", index, query);
        return Collections.emptyList();
    }

    @Override
    public List<String> vectorSearch(String index, float[] vector, int k) {
        log.warn("[NoOpSearchClient] vectorSearch called — ES not configured. index={}, k={}", index, k);
        return Collections.emptyList();
    }

    @Override
    public void indexDocument(String index, String docId, Map<String, Object> source) {
        log.warn("[NoOpSearchClient] indexDocument called — ES not configured. index={}, docId={}", index, docId);
        throw new SearchUnavailableException("检索服务未启用，索引写入被拒绝（index=" + index + ", docId=" + docId + "）");
    }

    @Override
    public void deleteDocument(String index, String docId) {
        log.warn("[NoOpSearchClient] deleteDocument called — ES not configured. index={}, docId={}", index, docId);
        throw new SearchUnavailableException("检索服务未启用，索引删除被拒绝（index=" + index + ", docId=" + docId + "）");
    }
}
