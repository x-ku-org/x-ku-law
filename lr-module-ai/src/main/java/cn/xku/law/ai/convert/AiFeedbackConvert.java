package cn.xku.law.ai.convert;

import cn.xku.law.ai.domain.AiFeedbackDO;
import cn.xku.law.ai.domain.dto.AiFeedbackCreateDTO;
import cn.xku.law.ai.domain.vo.AiFeedbackVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiFeedbackConvert {

    AiFeedbackVO toVO(AiFeedbackDO entity);

    List<AiFeedbackVO> toVOList(List<AiFeedbackDO> list);

    AiFeedbackDO toDO(AiFeedbackCreateDTO dto);
}
