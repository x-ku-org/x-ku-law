package cn.xku.law.auth;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户的完整账号资料（GET /account/profile），
 * 比 {@link CurrentUserVO} 更厚，含邮箱/手机/头像/性别与安全信息，供账号设置页概览与资料编辑使用。
 */
public record AccountProfileVO(
        Long userId,
        String username,
        String realName,
        String nickname,
        String email,
        String mobile,
        String gender,
        String avatarUrl,
        String userType,
        String tenantCode,
        String tenantName,
        List<String> roles,
        LocalDateTime lastLoginTime,
        String lastLoginIp,
        LocalDateTime passwordUpdateTime,
        LocalDateTime createTime
) {}
