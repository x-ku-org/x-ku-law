package cn.xku.law.common.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class SearchQuery {
    private String index;
    private String keyword;
    /** 字段精确过滤条件（字段名 → 值） */
    private Map<String, Object> filters;
    private int from;
    private int size;
    /** 高亮字段，为空时使用索引默认高亮字段 */
    private String[] highlightFields;
    /** 可选聚合规格；为空则不执行聚合（聚合作用于全结果集，不受 from/size 限制） */
    private SearchAggSpec aggSpec;
    /** 排序字段；为空时由检索实现决定默认排序（有关键词→相关度，无关键词→按生效日期倒序） */
    private String sortField;
    /** 排序方向：asc / desc；为空按 desc 处理 */
    private String sortOrder;

    public SearchQuery(String index, String keyword, Map<String, Object> filters,
                       int from, int size, String[] highlightFields) {
        this.index = index;
        this.keyword = keyword;
        this.filters = filters;
        this.from = from;
        this.size = size;
        this.highlightFields = highlightFields;
    }
}
