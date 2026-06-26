package cn.xku.law.subscription.convert;

import cn.xku.law.subscription.domain.SubscriptionMatchDO;
import cn.xku.law.subscription.domain.vo.SubscriptionMatchVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMatchConvert {

    @Mapping(target = "documentTitle", source = "titleSnapshot")
    SubscriptionMatchVO toVO(SubscriptionMatchDO entity);

    List<SubscriptionMatchVO> toVOList(List<SubscriptionMatchDO> list);
}
