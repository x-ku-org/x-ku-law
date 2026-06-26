package cn.xku.law.system.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.RoleDO;
import cn.xku.law.system.domain.dto.RoleCreateDTO;
import cn.xku.law.system.domain.dto.RoleQueryDTO;
import cn.xku.law.system.domain.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RoleService extends IService<RoleDO> {
    PageResult<RoleVO> pageRoles(RoleQueryDTO query);
    RoleVO getRoleById(Long id);
    Long createRole(RoleCreateDTO dto);
    void updateRole(Long id, RoleCreateDTO dto);
    void removeRole(Long id);

    /** 角色当前已分配的权限 ID 列表。 */
    List<Long> getPermissionIds(Long roleId);

    /** 覆盖式为角色分配权限，并失效持有该角色的用户权限缓存。 */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}
