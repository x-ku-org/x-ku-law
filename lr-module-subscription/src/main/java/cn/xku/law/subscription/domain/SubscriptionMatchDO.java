package cn.xku.law.subscription.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订阅命中，对应 lr_subscription_match */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_subscription_match")
public class SubscriptionMatchDO extends BaseDO {

    private Long ruleId;
    private Long documentId;
    private Long versionId;
    private Long articleId;
    /** 命中法规标题快照（生成命中时落库，列表直接展示，免跨模块联表） */
    private String titleSnapshot;
    /** new/update/repeal/keyword */
    private String matchType;
    private String matchReason;
    private BigDecimal matchScore;
    private LocalDateTime matchedTime;
    /** unread/read */
    private String readStatus;
}
