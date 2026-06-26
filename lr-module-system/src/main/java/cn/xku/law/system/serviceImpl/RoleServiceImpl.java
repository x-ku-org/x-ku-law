package cn.xku.law.system.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.PermissionCacheManager;
import cn.xku.law.system.convert.RoleConvert;
import cn.xku.law.system.domain.RoleDO;
import cn.xku.law.system.domain.RolePermissionDO;
import cn.xku.law.system.domain.dto.RoleCreateDTO;
import cn.xku.law.system.domain.dto.RoleQueryDTO;
import cn.xku.law.system.domain.vo.RoleVO;
import cn.xku.law.system.mapper.RoleMapper;
import cn.xku.law.system.mapper.RolePermissionMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import cn.xku.law.system.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleDO> implements RoleService {

    private final RoleConvert convert;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionCacheManager permissionCacheManager;

    @Override
    public PageResult<RoleVO> pageRoles(RoleQueryDTO query) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<RoleDO>()
                .like(StringUtils.hasText(query.getRoleCode()), RoleDO::getRoleCode, query.getRoleCode())
                .like(StringUtils.hasText(query.getRoleName()), RoleDO::getRoleName, query.getRoleName())
                .eq(StringUtils.hasText(query.getStatus()), RoleDO::getStatus, query.getStatus())
                .orderByAsc(RoleDO::getSortOrder);
        IPage<RoleDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public RoleVO getRoleById(Long id) {
        RoleDO role = this.getById(id);
        if (role == null) throw new AppException(ErrorCode.NOT_FOUND);
        return convert.toVO(role);
    }

    @Override
    public Long createRole(RoleCreateDTO dto) {
        RoleDO role = convert.toDO(dto);
        this.save(role);
        return role.getId();
    }

    @Override
    public void updateRole(Long id, RoleCreateDTO dto) {
        RoleDO role = this.getById(id);
        if (role == null) throw new AppException(ErrorCode.NOT_FOUND);
        convert.updateDO(dto, role);
        this.updateById(role);
        // 角色变更（如停用、改名）可能影响其权限码集合，失效持有该角色的用户缓存
        permissionCacheManager.evictUsers(userRoleMapper.selectUserIdsByRoleId(id));
    }

    @Override
    public void removeRole(Long id) {
        List<Long> affectedUserIds = userRoleMapper.selectUserIdsByRoleId(id);
        if (!this.removeById(id)) throw new AppException(ErrorCode.NOT_FOUND);
        permissionCacheManager.evictUsers(affectedUserIds);
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        return rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermissionDO>().eq(RolePermissionDO::getRoleId, roleId))
                .stream()
                .map(RolePermissionDO::getPermissionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        RoleDO role = this.getById(roleId);
        if (role == null) throw new AppException(ErrorCode.NOT_FOUND);
        rolePermissionMapper.physicalDeleteByRoleId(roleId);
        if (permissionIds != null) {
            permissionIds.stream().filter(Objects::nonNull).distinct().forEach(permissionId -> {
                RolePermissionDO link = new RolePermissionDO();
                link.setRoleId(roleId);
                link.setPermissionId(permissionId);
                rolePermissionMapper.insert(link);
            });
        }
        // 权限集合变化，失效持有该角色的所有用户缓存
        permissionCacheManager.evictUsers(userRoleMapper.selectUserIdsByRoleId(roleId));
    }
}
