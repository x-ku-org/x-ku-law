package cn.xku.law.system.convert;

import cn.xku.law.system.domain.RoleDO;
import cn.xku.law.system.domain.dto.RoleCreateDTO;
import cn.xku.law.system.domain.vo.RoleVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleConvert {
    RoleVO toVO(RoleDO role);
    List<RoleVO> toVOList(List<RoleDO> roles);
    RoleDO toDO(RoleCreateDTO dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(RoleCreateDTO dto, @MappingTarget RoleDO role);
}
