package cn.xku.law.system.mapper;

import cn.xku.law.system.domain.UserRoleDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {

    /**
     * 物理删除某用户的全部角色关联（覆盖式重新分配前清场）。
     * 唯一键 uk_user_role(tenant_id,user_id,role_id) 不含 deleted，逻辑删除后再插同一对会撞唯一键，
     * 故此处用物理删除；租户条件由多租户插件自动追加。
     */
    @Delete("DELETE FROM lr_user_role WHERE user_id = #{userId}")
    int physicalDeleteByUserId(@Param("userId") Long userId);

    /**
     * 登录时权限链查询：绕过 TenantLineHandler（登录时 SecurityContext 无租户信息），
     * 显式传入 tenantId，查 lr_user_role→lr_role→lr_role_permission→lr_permission。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT DISTINCT p.permission_code " +
            "FROM lr_user_role ur " +
            "JOIN lr_role r ON ur.role_id = r.id AND r.deleted = 0 " +
            "JOIN lr_role_permission rp ON r.id = rp.role_id AND rp.deleted = 0 " +
            "JOIN lr_permission p ON rp.permission_id = p.id AND p.deleted = 0 " +
            "WHERE ur.user_id = #{userId} AND ur.tenant_id = #{tenantId} AND ur.deleted = 0 " +
            "AND r.status = 'enabled' AND p.status = 'enabled'")
    List<String> selectPermissionCodes(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /** 当前用户的启用角色码集合（GET /auth/me 用）。绕过租户过滤，显式传 tenantId，范式同上。 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT DISTINCT r.role_code " +
            "FROM lr_user_role ur " +
            "JOIN lr_role r ON ur.role_id = r.id AND r.deleted = 0 " +
            "WHERE ur.user_id = #{userId} AND ur.tenant_id = #{tenantId} AND ur.deleted = 0 " +
            "AND r.status = 'enabled'")
    List<String> selectRoleCodes(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 权限缓存失效用：查持有某角色的所有用户。绕过租户过滤以保证“受影响用户”覆盖完整
     * （宁可多失效一次缓存——仅造成一次重载——也不漏失效导致权限变更不生效）。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT DISTINCT user_id FROM lr_user_role WHERE role_id = #{roleId} AND deleted = 0")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /** 权限缓存失效用：查持有任一指定角色的所有用户。绕过租户过滤，理由同上。 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("<script>SELECT DISTINCT user_id FROM lr_user_role WHERE deleted = 0 AND role_id IN " +
            "<foreach item='id' collection='roleIds' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Long> selectUserIdsByRoleIds(@Param("roleIds") Collection<Long> roleIds);
}
