package cn.xku.law.auth;

/** 认证业务接口：登录 / 刷新 / 登出 */
public interface AuthService {

    LoginResponse login(LoginRequest request, String ip, String userAgent);

    /** 自助注册：创建 normal 普通用户（落默认租户 platform）并自动登录返回令牌。 */
    LoginResponse register(RegisterRequest request, String ip, String userAgent);

    String refresh(String refreshToken);

    void logout(String accessToken);

    /** 当前登录用户信息（资料 + 角色码 + 权限码），供前端门禁与个性化展示。 */
    CurrentUserVO currentUser();
}
