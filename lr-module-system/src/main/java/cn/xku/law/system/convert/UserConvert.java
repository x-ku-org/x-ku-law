package cn.xku.law.system.convert;

import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.domain.dto.UserCreateDTO;
import cn.xku.law.system.domain.dto.UserUpdateDTO;
import cn.xku.law.system.domain.vo.UserVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvert {
    UserVO toVO(UserDO user);
    List<UserVO> toVOList(List<UserDO> users);
    UserDO toDO(UserCreateDTO dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDO(UserUpdateDTO dto, @MappingTarget UserDO user);
}
