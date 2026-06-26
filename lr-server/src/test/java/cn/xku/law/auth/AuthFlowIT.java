package cn.xku.law.auth;

import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.security.PermissionCacheManager;
import cn.xku.law.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证闭环集成测试，覆盖：
 *  - 登录后访问受保护接口；
 *  - 回归 #5（P1-1）：perms 缓存过期后，用 refresh 换新 access token 仍能正常访问（不再沦为零权限令牌）；
 *  - 回归 #7（P1-5）：多端登录时，一端登出不影响另一端。
 */
class AuthFlowIT extends AbstractIntegrationTest {

    private static final String PROTECTED_URI = "/system/users";

    @Autowired
    private PermissionCacheManager permissionCacheManager;

    @Test
    void loginThenAccessProtectedEndpoint() throws Exception {
        TokenPair tokens = loginAdmin();
        getJson(PROTECTED_URI, tokens.accessToken()).andExpect(status().isOk());
    }

    @Test
    void refreshRebuildsPermissionsAfterPermsCacheExpiry() throws Exception {
        TokenPair tokens = loginAdmin();

        // 正常访问
        getJson(PROTECTED_URI, tokens.accessToken()).andExpect(status().isOk());

        redisTemplate.delete(SecurityConstants.REDIS_PERMS_PREFIX + 1L);

        // 此时同一 access token 因取不到权限 → 403（这正是 #5 的故障条件）
        getJson(PROTECTED_URI, tokens.accessToken()).andExpect(status().isUnauthorized());

        String newAccess = refreshAccess(tokens.refreshToken());
        assertThat(newAccess).isNotBlank();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_PERMS_PREFIX + 1L)).isTrue();

        // 新 access token 访问受保护接口恢复正常（回归断言）
        getJson(PROTECTED_URI, newAccess).andExpect(status().isOk());
    }

    @Test
    void permissionInvalidationRebuildsCacheForActiveAccessToken() throws Exception {
        TokenPair tokens = loginAdmin();

        getJson(PROTECTED_URI, tokens.accessToken()).andExpect(status().isOk());

        permissionCacheManager.evictUser(1L);

        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_PERMS_PREFIX + 1L)).isTrue();
        getJson(PROTECTED_URI, tokens.accessToken()).andExpect(status().isOk());
    }

    @Test
    void refreshRecreatesUserRefreshSetWhenItExpired() throws Exception {
        TokenPair deviceA = loginAdmin();
        TokenPair deviceB = loginAdmin();

        String userRefreshKey = SecurityConstants.REDIS_USER_REFRESH_PREFIX + 1L;
        redisTemplate.delete(userRefreshKey);

        String bNewAccess = refreshAccess(deviceB.refreshToken());

        assertThat(redisTemplate.opsForSet().members(userRefreshKey)).contains(deviceB.refreshToken());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/auth/logout")
                        .header("Authorization", "Bearer " + deviceA.accessToken()))
                .andExpect(status().isOk());

        getJson(PROTECTED_URI, bNewAccess).andExpect(status().isOk());
    }

    @Test
    void refreshRejectsDisabledUser() throws Exception {
        long userId = insertUser("disabled_refresh", "Pwd@123", PLATFORM_TENANT_ID);
        TokenPair tokens = login(PLATFORM_TENANT_CODE, "disabled_refresh", "Pwd@123");

        jdbcTemplate.update("UPDATE lr_user SET status='disabled' WHERE id=?", userId);

        mockMvc.perform(post("/auth/refresh").header("X-Refresh-Token", tokens.refreshToken()))
                .andExpect(status().isForbidden());
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_REFRESH_PREFIX + tokens.refreshToken())).isFalse();
    }

    @Test
    void userUpdateEncodesPasswordReset() throws Exception {
        long userId = insertUser("reset_user", "Old@123", PLATFORM_TENANT_ID);
        TokenPair admin = loginAdmin();

        putJson("/system/users/" + userId, admin.accessToken(),
                Map.of("username", "reset_user", "password", "New@123", "status", "enabled"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tenantCode", PLATFORM_TENANT_CODE,
                                "username", "reset_user",
                                "password", "Old@123"))))
                .andExpect(status().isUnauthorized());

        TokenPair resetUser = login(PLATFORM_TENANT_CODE, "reset_user", "New@123");
        assertThat(resetUser.accessToken()).isNotBlank();

        String requestParams = jdbcTemplate.queryForObject(
                "SELECT request_params FROM lr_operation_log WHERE request_url=? ORDER BY id DESC LIMIT 1",
                String.class,
                "/system/users/" + userId);
        assertThat(requestParams).doesNotContain("New@123").contains("\"password\":\"***\"");
    }

    @Test
    void userDisableRevokesExistingAccessAndRefreshSessions() throws Exception {
        long userId = insertUser("disable_session", "Pwd@123", PLATFORM_TENANT_ID);
        TokenPair user = login(PLATFORM_TENANT_CODE, "disable_session", "Pwd@123");
        TokenPair admin = loginAdmin();

        putJson("/system/users/" + userId, admin.accessToken(),
                Map.of("status", "disabled"))
                .andExpect(status().isOk());

        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_SESSION_PREFIX + user.accessToken())).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_REFRESH_PREFIX + user.refreshToken())).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_USER_ACCESS_PREFIX + userId)).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_USER_REFRESH_PREFIX + userId)).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_PERMS_PREFIX + userId)).isFalse();

        getJson(PROTECTED_URI, user.accessToken()).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh").header("X-Refresh-Token", user.refreshToken()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void userDeleteRevokesExistingAccessAndRefreshSessions() throws Exception {
        long userId = insertUser("delete_session", "Pwd@123", PLATFORM_TENANT_ID);
        TokenPair user = login(PLATFORM_TENANT_CODE, "delete_session", "Pwd@123");
        TokenPair admin = loginAdmin();

        deleteJson("/system/users/" + userId, admin.accessToken()).andExpect(status().isOk());

        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_SESSION_PREFIX + user.accessToken())).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_REFRESH_PREFIX + user.refreshToken())).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_USER_ACCESS_PREFIX + userId)).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_USER_REFRESH_PREFIX + userId)).isFalse();
        assertThat(redisTemplate.hasKey(SecurityConstants.REDIS_PERMS_PREFIX + userId)).isFalse();

        getJson(PROTECTED_URI, user.accessToken()).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh").header("X-Refresh-Token", user.refreshToken()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void userUpdateWithoutPasswordPreservesCurrentPassword() throws Exception {
        long userId = insertUser("profile_user", "Old@123", PLATFORM_TENANT_ID);
        TokenPair admin = loginAdmin();

        putJson("/system/users/" + userId, admin.accessToken(),
                Map.of("realName", "Profile User", "status", "enabled"))
                .andExpect(status().isOk());

        TokenPair profileUser = login(PLATFORM_TENANT_CODE, "profile_user", "Old@123");
        assertThat(profileUser.accessToken()).isNotBlank();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT real_name FROM lr_user WHERE id=?", String.class, userId))
                .isEqualTo("Profile User");
    }

    @Test
    void logoutOneDeviceDoesNotAffectAnother() throws Exception {
        TokenPair deviceA = loginAdmin();
        TokenPair deviceB = loginAdmin();
        assertThat(deviceA.refreshToken()).isNotEqualTo(deviceB.refreshToken());

        // 两端均可访问
        getJson(PROTECTED_URI, deviceA.accessToken()).andExpect(status().isOk());
        getJson(PROTECTED_URI, deviceB.accessToken()).andExpect(status().isOk());

        // A 端登出
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/auth/logout")
                        .header("Authorization", "Bearer " + deviceA.accessToken()))
                .andExpect(status().isOk());

        // A 端 access 会话失效
        getJson(PROTECTED_URI, deviceA.accessToken()).andExpect(status().isUnauthorized());
        // A 端 refresh 被精确撤销
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/auth/refresh").header("X-Refresh-Token", deviceA.refreshToken()))
                .andExpect(status().is4xxClientError());

        getJson(PROTECTED_URI, deviceB.accessToken()).andExpect(status().isOk());
        String bNewAccess = refreshAccess(deviceB.refreshToken());
        getJson(PROTECTED_URI, bNewAccess).andExpect(status().isOk());
    }
}
