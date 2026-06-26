package cn.xku.law.ai.convert;

import cn.xku.law.ai.domain.AiSessionDO;
import cn.xku.law.ai.domain.vo.AiSessionVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiSessionConvert {

    @Mapping(target = "title", source = "sessionTitle")
    AiSessionVO toVO(AiSessionDO entity);

    List<AiSessionVO> toVOList(List<AiSessionDO> list);
}
