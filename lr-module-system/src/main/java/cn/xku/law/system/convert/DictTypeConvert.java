package cn.xku.law.system.convert;

import cn.xku.law.system.domain.DictTypeDO;
import cn.xku.law.system.domain.dto.DictTypeCreateDTO;
import cn.xku.law.system.domain.vo.DictTypeVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictTypeConvert {
    DictTypeVO toVO(DictTypeDO dictType);
    List<DictTypeVO> toVOList(List<DictTypeDO> list);
    DictTypeDO toDO(DictTypeCreateDTO dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(DictTypeCreateDTO dto, @MappingTarget DictTypeDO dictType);
}
