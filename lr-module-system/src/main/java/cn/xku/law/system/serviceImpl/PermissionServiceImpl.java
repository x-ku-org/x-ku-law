package cn.xku.law.system.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.PermissionCacheManager;
import cn.xku.law.system.convert.PermissionConvert;
import cn.xku.law.system.domain.PermissionDO;
import cn.xku.law.system.domain.dto.PermissionCreateDTO;
import cn.xku.law.system.domain.dto.PermissionQueryDTO;
import cn.xku.law.system.domain.vo.PermissionVO;
import cn.xku.law.system.mapper.PermissionMapper;
import cn.xku.law.system.mapper.RolePermissionMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import cn.xku.law.system.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, PermissionDO>
        implements PermissionService {

    private final PermissionConvert convert;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionCacheManager permissionCacheManager;

    @Override
    public PageResult<PermissionVO> pagePermissions(PermissionQueryDTO query) {
        LambdaQueryWrapper<PermissionDO> wrapper = new LambdaQueryWrapper<PermissionDO>()
                .eq(query.getParentId() != null, PermissionDO::getParentId, query.getParentId())
                .eq(StringUtils.hasText(query.getPermissionType()), PermissionDO::getPermissionType, query.getPermissionType())
                .like(StringUtils.hasText(query.getKeyword()), PermissionDO::getPermissionName, query.getKeyword())
                .eq(StringUtils.hasText(query.getStatus()), PermissionDO::getStatus, query.getStatus())
                .orderByAsc(PermissionDO::getSortOrder);
        IPage<PermissionDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public List<PermissionVO> listAll() {
        return convert.toVOList(this.list(new LambdaQueryWrapper<PermissionDO>().orderByAsc(PermissionDO::getSortOrder)));
    }

    @Override
    public Long createPermission(PermissionCreateDTO dto) {
        PermissionDO permission = convert.toDO(dto);
        this.save(permission);
        return permission.getId();
    }

    @Override
    public void updatePermission(Long id, PermissionCreateDTO dto) {
        PermissionDO permission = this.getById(id);
        if (permission == null) throw new AppException(ErrorCode.NOT_FOUND);
        convert.updateDO(dto, permission);
        this.updateById(permission);
        evictUsersHavingPermission(id);
    }

    @Override
    public void removePermission(Long id) {
        List<Long> affectedUserIds = resolveUsersHavingPermission(id);
        if (!this.removeById(id)) throw new AppException(ErrorCode.NOT_FOUND);
        permissionCacheManager.evictUsers(affectedUserIds);
    }

    /** permission → 引用它的角色 → 持有这些角色的用户，失效其权限缓存。 */
    private void evictUsersHavingPermission(Long permissionId) {
        permissionCacheManager.evictUsers(resolveUsersHavingPermission(permissionId));
    }

    private List<Long> resolveUsersHavingPermission(Long permissionId) {
        List<Long> roleIds = rolePermissionMapper.selectRoleIdsByPermissionId(permissionId);
        if (roleIds.isEmpty()) return List.of();
        return userRoleMapper.selectUserIdsByRoleIds(roleIds);
    }
}
