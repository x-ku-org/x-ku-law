package cn.xku.law.search.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 效力层级 × 年份分布桶，跨全部命中（不受分页限制），供前端绘制分布矩阵。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixBucketVO {
    /** 效力层级（lr_law_document.legal_level 原始码值） */
    private String effectLevel;
    private int year;
    private long count;
}
