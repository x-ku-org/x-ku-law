package cn.xku.law.subscription.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionRuleQueryDTO extends PageParam {
    private String ruleType;
    private String status;
}
