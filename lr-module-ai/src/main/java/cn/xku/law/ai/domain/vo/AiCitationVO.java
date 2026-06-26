package cn.xku.law.ai.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** AI 回答引用依据，前端证据账册展示用。 */
@Data
public class AiCitationVO {
    /** 引用 ID（字符串，前端用作 cite 标识） */
    private String id;
    /** 来源法规标题 */
    private String source;
    /** 条款标签，如 第十二条 */
    private String article;
    /** 引文摘录 */
    private String excerpt;
    /** 置信度 */
    private BigDecimal confidence;
    /** 关联法规文档 ID，供前端跳转正文 */
    private Long documentId;
    /** 时效：current（现行有效）/ superseded（历史版本）/ repealed（已废止/失效），供前端标注 */
    private String validityStatus;
}
