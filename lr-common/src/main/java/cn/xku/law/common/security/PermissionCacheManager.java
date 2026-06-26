package cn.xku.law.common.security;

import java.util.List;
import java.util.Collection;

/**
 * 权限缓存失效器：RBAC（角色 / 权限 / 用户角色）变更后，主动失效受影响用户的 {@code perms:} 缓存，
 * 使变更即时生效，而非等待缓存 TTL（= access token TTL，默认 2h）自然过期。
 * <p>真实实现在 lr-server（基于 Redis）。这里以接口下沉到 lr-common，供各业务域服务依赖反转调用。
 */
public interface PermissionCacheManager {

    /** 失效一批用户的权限缓存；忽略 null / 空入参。 */
    void evictUsers(Collection<Long> userIds);

    /** 失效单个用户的权限缓存。 */
    default void evictUser(Long userId) {
        if (userId != null) {
            evictUsers(List.of(userId));
        }
    }
}
