package cn.xku.law.subscription.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionMatchQueryDTO extends PageParam {
    private Long ruleId;
    private String readStatus;
}
