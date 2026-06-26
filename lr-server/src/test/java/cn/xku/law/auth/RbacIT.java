package cn.xku.law.auth;

import cn.xku.law.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RBAC 接口鉴权集成测试，覆盖：
 *  - 无权限普通用户访问管理接口 403、管理员 200；
 *  - 回归 #6（P1-4）：管理员变更角色/权限后，受影响用户缓存即时失效，不再等 2h。
 */
class RbacIT extends AbstractIntegrationTest {

    @Test
    void normalUserForbiddenAdminAllowed() throws Exception {
        long carol = insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);
        // carol 无任何角色 → 空权限
        TokenPair carolTokens = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");
        getJson("/system/users", carolTokens.accessToken()).andExpect(status().isForbidden());
        deleteJson("/system/roles/1", carolTokens.accessToken()).andExpect(status().isForbidden());

        TokenPair admin = loginAdmin();
        getJson("/system/users", admin.accessToken()).andExpect(status().isOk());
    }

    @Test
    void seededAdminCanListNotifications() throws Exception {
        TokenPair admin = loginAdmin();
        getJson("/system/notifications", admin.accessToken()).andExpect(status().isOk());
    }

    @Test
    void permissionChangeTakesEffectImmediately() throws Exception {
        long roleId = insertRole("test_viewer", PLATFORM_TENANT_ID);
        bindRolePermission(roleId, permissionId("system:user:list"), PLATFORM_TENANT_ID);
        long bob = insertUser("bob", "Bob@123", PLATFORM_TENANT_ID);
        bindUserRole(bob, roleId, PLATFORM_TENANT_ID);

        TokenPair bobTokens = login(PLATFORM_TENANT_CODE, "bob", "Bob@123");
        getJson("/system/users", bobTokens.accessToken()).andExpect(status().isOk());

        // 管理员删除该角色 —— P1-4 应立即失效 bob 的权限缓存
        TokenPair admin = loginAdmin();
        deleteJson("/system/roles/" + roleId, admin.accessToken()).andExpect(status().isOk());

        // bob 同一 access token 的下一次请求即刻失去权限（不需等缓存 TTL 过期）
        getJson("/system/users", bobTokens.accessToken()).andExpect(status().isForbidden());
    }
}
