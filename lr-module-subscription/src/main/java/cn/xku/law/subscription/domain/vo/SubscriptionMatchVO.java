package cn.xku.law.subscription.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionMatchVO {
    private Long id;
    private Long ruleId;
    private Long documentId;
    private Long versionId;
    private Long articleId;
    /** 命中法规标题（来自命中时的标题快照） */
    private String documentTitle;
    private String matchType;
    private String matchReason;
    private BigDecimal matchScore;
    private LocalDateTime matchedTime;
    private String readStatus;
    private LocalDateTime createTime;
}
