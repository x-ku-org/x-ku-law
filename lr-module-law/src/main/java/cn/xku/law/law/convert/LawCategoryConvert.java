package cn.xku.law.law.convert;

import cn.xku.law.law.domain.LawCategoryDO;
import cn.xku.law.law.domain.dto.LawCategoryCreateDTO;
import cn.xku.law.law.domain.vo.LawCategoryVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LawCategoryConvert {

    LawCategoryVO toVO(LawCategoryDO entity);

    List<LawCategoryVO> toVOList(List<LawCategoryDO> list);

    LawCategoryDO toDO(LawCategoryCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(LawCategoryCreateDTO dto, @MappingTarget LawCategoryDO entity);
}
