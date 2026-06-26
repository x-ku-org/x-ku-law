package cn.xku.law.subscription.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 订阅规则，对应 lr_subscription_rule */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_subscription_rule")
public class SubscriptionRuleDO extends BaseDO {

    private Long userId;
    private String ruleName;
    /** law_update/keyword/topic/region/industry */
    private String ruleType;
    private String keyword;
    private String filtersJson;
    /** station/email/sms/webhook */
    private String deliveryChannel;
    /** realtime/daily/weekly */
    private String frequency;
    private String status;
    private java.time.LocalDateTime lastRunTime;
}
