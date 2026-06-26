package cn.xku.law.system.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.domain.dto.UserCreateDTO;
import cn.xku.law.system.domain.dto.UserQueryDTO;
import cn.xku.law.system.domain.dto.UserUpdateDTO;
import cn.xku.law.system.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserService extends IService<UserDO> {
    PageResult<UserVO> pageUsers(UserQueryDTO query);
    UserVO getUserById(Long id);
    Long createUser(UserCreateDTO dto);
    void updateUser(Long id, UserUpdateDTO dto);
    void removeUser(Long id);

    /** 用户当前已分配的角色 ID 列表。 */
    List<Long> getRoleIds(Long userId);

    /** 覆盖式为用户分配角色，并失效该用户权限缓存。 */
    void assignRoles(Long userId, List<Long> roleIds);
}
