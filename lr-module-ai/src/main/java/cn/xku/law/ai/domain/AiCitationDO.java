package cn.xku.law.ai.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** AI 引用依据，对应 lr_ai_citation */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_ai_citation")
public class AiCitationDO extends BaseDO {

    private Long messageId;
    /** law_document/law_article/chunk */
    private String refType;
    private Long refId;
    private Long documentId;
    private Long versionId;
    private Long articleId;
    private String quoteText;
    private Integer citationOrder;
    private BigDecimal confidenceScore;
    private Boolean verifiedFlag;
    /** 来源法规标题（落库冗余，便于历史回读展示） */
    private String sourceTitle;
    /** 条款标签，如 第十二条（落库冗余） */
    private String articleLabel;
    /** 时效：current（现行有效）/ superseded（历史版本）/ repealed（已废止/失效） */
    private String validityStatus;
}
