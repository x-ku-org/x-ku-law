package cn.xku.law.law.diff;

/** 逐条对比中单条条款相对基准版本的变化类型。 */
public enum ArticleChangeType {
    /** 仅目标版本存在（新增条款）。 */
    ADDED,
    /** 仅基准版本存在（删除条款）。 */
    REMOVED,
    /** 两版本都有但正文不同（修改条款）。 */
    MODIFIED,
    /** 两版本正文一致（未变）。 */
    UNCHANGED
}
