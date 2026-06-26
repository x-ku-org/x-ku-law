package cn.xku.law.system.convert;

import cn.xku.law.system.domain.DictDataDO;
import cn.xku.law.system.domain.dto.DictDataCreateDTO;
import cn.xku.law.system.domain.vo.DictDataVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictDataConvert {
    DictDataVO toVO(DictDataDO dictData);
    List<DictDataVO> toVOList(List<DictDataDO> list);
    DictDataDO toDO(DictDataCreateDTO dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(DictDataCreateDTO dto, @MappingTarget DictDataDO dictData);
}
