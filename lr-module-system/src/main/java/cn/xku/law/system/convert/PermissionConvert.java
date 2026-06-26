package cn.xku.law.system.convert;

import cn.xku.law.system.domain.PermissionDO;
import cn.xku.law.system.domain.dto.PermissionCreateDTO;
import cn.xku.law.system.domain.vo.PermissionVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionConvert {
    PermissionVO toVO(PermissionDO permission);
    List<PermissionVO> toVOList(List<PermissionDO> permissions);
    PermissionDO toDO(PermissionCreateDTO dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(PermissionCreateDTO dto, @MappingTarget PermissionDO permission);
}
