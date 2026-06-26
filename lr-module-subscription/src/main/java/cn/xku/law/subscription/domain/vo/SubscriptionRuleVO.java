package cn.xku.law.subscription.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionRuleVO {
    private Long id;
    private Long userId;
    private String ruleName;
    private String ruleType;
    private String keyword;
    private String filtersJson;
    private String deliveryChannel;
    private String frequency;
    private String status;
    private LocalDateTime lastRunTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
