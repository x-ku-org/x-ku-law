package cn.xku.law.search.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 法规检索分页结果，在标准分页字段之上附带「全结果集」统计：
 * {@code documentCount}（去重文件数）与 {@code matrix}（效力层级 × 年份分布）。
 * 这两项均由 ES 聚合得出，作用于整个过滤结果集，不随当前页变化。
 */
@Data
public class LawSearchPageVO {
    /** 命中总数（ES 对全结果集的估计） */
    private long total;
    /** 当前页结果 */
    private List<LawSearchResultVO> list = new ArrayList<>();
    /** 本页被回查可信源剔脏的条数 */
    private int filteredCount;
    /** 去重文件数（按 documentId 去重，跨全部命中） */
    private long documentCount;
    /** 效力层级 × 年份分布（跨全部命中） */
    private List<MatrixBucketVO> matrix = new ArrayList<>();
}
