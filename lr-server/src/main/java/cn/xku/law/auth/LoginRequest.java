package cn.xku.law.auth;

import jakarta.validation.constraints.NotBlank;

/** 登录请求体 */
public record LoginRequest(
        // 当前对外隐藏租户：tenantCode 可选，为空时后端回落到默认租户 platform。
        // 将来开放多租户时恢复 @NotBlank 校验。
        String tenantCode,
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password
) {}
