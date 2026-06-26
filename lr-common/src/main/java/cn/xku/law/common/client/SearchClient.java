package cn.xku.law.common.client;

import cn.xku.law.common.client.dto.SearchPage;
import cn.xku.law.common.client.dto.SearchQuery;
import cn.xku.law.common.client.dto.VectorMatch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 统一搜索客户端接口，承担全文检索与语义/向量 kNN 检索两类职责。
 */
public interface SearchClient {

    /**
     * 全文检索（带高亮），返回 SearchPage（含文档 ID + 高亮片段）。
     * 实现类在 ES 不可用时抛出 SearchUnavailableException。
     */
    SearchPage search(SearchQuery query);

    /**
     * 全文检索，返回文档 ID 列表（向后兼容简化接口）。
     */
    List<String> search(String index, String query, Map<String, Object> filters, int from, int size);

    /**
     * 向量 kNN 检索，返回最近邻文档 ID 列表。
     *
     * @param index  ES 索引名
     * @param vector 查询向量
     * @param k      返回 top-k 结果数
     */
    List<String> vectorSearch(String index, float[] vector, int k);

    /**
     * 向量 kNN 检索，返回最近邻文档 ID + 相似度分值（按分值降序，等同 ES `_score`）。
     * 默认实现返回空列表；具备语义检索能力的实现应覆盖以回传真实分值。
     */
    default List<VectorMatch> vectorSearchScored(String index, float[] vector, int k) {
        return Collections.emptyList();
    }

    /** 索引单个文档（新增或覆盖） */
    void indexDocument(String index, String docId, Map<String, Object> source);

    /** 删除单个文档 */
    void deleteDocument(String index, String docId);
}
