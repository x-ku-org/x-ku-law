package cn.xku.law.law.convert;

import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.law.domain.vo.LawVersionVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LawVersionConvert {

    LawVersionVO toVO(LawVersionDO entity);

    List<LawVersionVO> toVOList(List<LawVersionDO> list);

    LawVersionDO toDO(LawVersionCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(LawVersionCreateDTO dto, @MappingTarget LawVersionDO entity);
}
