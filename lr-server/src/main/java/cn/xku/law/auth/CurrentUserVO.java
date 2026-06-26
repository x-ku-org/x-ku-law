package cn.xku.law.auth;

import java.util.List;

/**
 * 当前登录用户信息（GET /auth/me）。
 * 前端据 roles/permissions 做管理员门禁与个性化展示：
 * roles 含 platform_admin 或 permissions 含任一 system:* 即视为管理员。
 */
public record CurrentUserVO(
        Long userId,
        String username,
        String realName,
        String userType,
        String avatarUrl,
        String tenantCode,
        String tenantName,
        List<String> roles,
        List<String> permissions
) {}
