package cn.xku.law.common.security;

import cn.xku.law.common.constant.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 从 Spring SecurityContext 读取当前登录用户的工具方法 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        return null;
    }

    public static String getCurrentUsername() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUsername() : SecurityConstants.ANONYMOUS_USER;
    }

    public static Long getCurrentTenantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getTenantId() : 0L;
    }

    public static Long getCurrentUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }
}
