package cn.xku.law.subscription.convert;

import cn.xku.law.subscription.domain.SubscriptionRuleDO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleCreateDTO;
import cn.xku.law.subscription.domain.vo.SubscriptionRuleVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionRuleConvert {

    SubscriptionRuleVO toVO(SubscriptionRuleDO entity);

    List<SubscriptionRuleVO> toVOList(List<SubscriptionRuleDO> list);

    SubscriptionRuleDO toDO(SubscriptionRuleCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(SubscriptionRuleCreateDTO dto, @MappingTarget SubscriptionRuleDO entity);
}
