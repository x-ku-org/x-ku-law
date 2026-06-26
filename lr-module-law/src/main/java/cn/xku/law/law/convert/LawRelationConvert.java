package cn.xku.law.law.convert;

import cn.xku.law.law.domain.LawRelationDO;
import cn.xku.law.law.domain.dto.LawRelationCreateDTO;
import cn.xku.law.law.domain.vo.LawRelationVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LawRelationConvert {

    LawRelationVO toVO(LawRelationDO entity);

    List<LawRelationVO> toVOList(List<LawRelationDO> list);

    LawRelationDO toDO(LawRelationCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(LawRelationCreateDTO dto, @MappingTarget LawRelationDO entity);
}
