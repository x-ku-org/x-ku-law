package cn.xku.law.law.diff;

import lombok.Data;

import java.util.List;

/** 两个版本之间的逐条对比结果。 */
@Data
public class VersionDiffResult {

    private Long baseVersionId;
    private Long targetVersionId;
    private int addedCount;
    private int removedCount;
    private int modifiedCount;
    private int unchangedCount;
    /** 变化条款数（新增+删除+修改），不含未变。 */
    private int changeCount;
    /** 人类可读摘要，如「新增3条、修改5条、删除1条」。 */
    private String summary;
    /** 逐条明细。默认包含全部对齐结果（含 UNCHANGED）。 */
    private List<ArticleChange> changes;
}
