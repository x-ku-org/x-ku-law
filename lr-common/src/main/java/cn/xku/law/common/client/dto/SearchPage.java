package cn.xku.law.common.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SearchPage {
    private long total;
    private List<SearchHit> hits;
    /** 去重计数（cardinality 聚合结果），未请求聚合时为 0。作用于全结果集。 */
    private long distinctCount;
    /** 「分组 × 年份」分布桶（作用于全结果集），未请求聚合时为空 */
    private List<GroupDateBucket> groupDateBuckets = new ArrayList<>();

    public SearchPage(long total, List<SearchHit> hits) {
        this.total = total;
        this.hits = hits;
    }
}
