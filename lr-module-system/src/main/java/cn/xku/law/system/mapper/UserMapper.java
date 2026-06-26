package cn.xku.law.system.mapper;

import cn.xku.law.system.domain.UserDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 登录专用查询：显式绕过 TenantLineHandler，因为登录时 SecurityContext 中尚无租户信息，
     * 不绕过会导致自动注入 tenant_id=0，查不到其他租户的用户。
     * 显式传入 tenantId 并手动追加 deleted=0 过滤（@Select 绕过了 TableLogic 插件）。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM lr_user WHERE username = #{username} AND tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    UserDO selectByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") Long tenantId);

    /** 权限缓存重建用：按用户 ID 查租户，绕过租户插件以覆盖跨租户 RBAC 变更。 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM lr_user WHERE id = #{userId} AND deleted = 0 LIMIT 1")
    UserDO selectActiveByIdIgnoreTenant(@Param("userId") Long userId);

    /**
     * 自助注册手机号唯一性预校验：显式按租户作用域，绕过租户插件（注册时未登录态无租户上下文）。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM lr_user WHERE mobile = #{mobile} AND tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    UserDO selectByMobileAndTenantId(@Param("mobile") String mobile, @Param("tenantId") Long tenantId);
}
