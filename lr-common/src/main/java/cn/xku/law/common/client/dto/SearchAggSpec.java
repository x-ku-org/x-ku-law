package cn.xku.law.common.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检索聚合规格（可选）。为空时检索不执行任何聚合，仅返回分页命中。
 * 聚合作用于整个结果集（不受分页 from/size 限制），用于全量统计与分布可视化。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchAggSpec {
    /** 去重计数字段（cardinality 聚合）。如 documentId → 跨全部命中的去重文件数。为空则不计。 */
    private String distinctField;
    /** 分组字段（terms 聚合）。如 effectLevel。与 dateField 同时存在时构成「分组 × 年份」分布。 */
    private String groupField;
    /** 日期字段（按年 date_histogram 子聚合）。如 effectiveDate。为空则不计分布。 */
    private String dateField;
}
