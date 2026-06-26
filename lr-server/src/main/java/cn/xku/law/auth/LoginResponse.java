package cn.xku.law.auth;

/** 登录成功响应 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
