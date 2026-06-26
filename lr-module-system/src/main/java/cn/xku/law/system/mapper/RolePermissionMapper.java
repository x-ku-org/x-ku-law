package cn.xku.law.system.mapper;

import cn.xku.law.system.domain.RolePermissionDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionDO> {

    /**
     * 物理删除某角色的全部权限关联（覆盖式重新分配前清场）。
     * 唯一键 uk_role_permission(tenant_id,role_id,permission_id) 不含 deleted，逻辑删除后再插同一对会撞唯一键，
     * 故此处用物理删除；租户条件由多租户插件自动追加。
     */
    @Delete("DELETE FROM lr_role_permission WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 权限缓存失效用：查引用了某权限的所有角色。绕过租户过滤以覆盖全部受影响角色
     * （lr_permission 为全局表，单个权限可被多租户角色引用）。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT DISTINCT role_id FROM lr_role_permission WHERE permission_id = #{permissionId} AND deleted = 0")
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);
}
