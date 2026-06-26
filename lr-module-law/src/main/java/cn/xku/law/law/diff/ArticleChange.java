package cn.xku.law.law.diff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 逐条对比结果中的单条变化项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleChange {

    /** 条款号（对齐键）；退化全文条款可能为空，此时用 articleOrder 对齐。 */
    private String articleNo;
    /** 条款标题，取目标版本优先、否则基准版本，便于前端展示。 */
    private String articleTitle;
    private ArticleChangeType changeType;
    /** 基准版本正文；ADDED 时为 null。 */
    private String baseText;
    /** 目标版本正文；REMOVED 时为 null。 */
    private String targetText;
}
