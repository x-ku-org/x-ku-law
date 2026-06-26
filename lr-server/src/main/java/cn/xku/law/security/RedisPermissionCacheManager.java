package cn.xku.law.security;

import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.security.PermissionCacheManager;
import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.mapper.UserMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** Redis-backed permission cache invalidation for online users. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPermissionCacheManager implements PermissionCacheManager {

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void evictUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> normalizedUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (normalizedUserIds.isEmpty()) {
            return;
        }
        for (Long userId : normalizedUserIds) {
            rebuildUserPermissions(userId);
        }
    }

    private void rebuildUserPermissions(Long userId) {
        String key = SecurityConstants.REDIS_PERMS_PREFIX + userId;
        try {
            UserDO user = userMapper.selectActiveByIdIgnoreTenant(userId);
            if (user == null || user.getTenantId() == null) {
                safeDelete(key);
                revokeUserSessions(userId);
                return;
            }
            if (!"enabled".equals(user.getStatus())) {
                safeDelete(key);
                revokeUserSessions(userId);
                return;
            }

            Set<String> permissions = new HashSet<>();
            List<String> codes = userRoleMapper.selectPermissionCodes(userId, user.getTenantId());
            if (codes != null) {
                permissions.addAll(codes);
            }
            writePermissions(key, permissions);
        } catch (Exception e) {
            safeDelete(key);
            log.warn("[PermissionCache] rebuild permission cache failed, userId={}: {}", userId, e.getMessage());
        }
    }

    private void writePermissions(String key, Set<String> permissions) throws Exception {
        redisTemplate.opsForValue().set(
                key,
                objectMapper.writeValueAsString(permissions),
                jwtTokenUtil.getAccessTtlSeconds(),
                TimeUnit.SECONDS);
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
            // Cache invalidation must not roll back the business write.
        }
    }

    private void revokeUserSessions(Long userId) {
        try {
            Set<String> keys = new HashSet<>();

            String userRefreshKey = SecurityConstants.REDIS_USER_REFRESH_PREFIX + userId;
            Set<String> refreshTokens = redisTemplate.opsForSet().members(userRefreshKey);
            if (refreshTokens != null) {
                for (String refreshToken : refreshTokens) {
                    keys.add(SecurityConstants.REDIS_REFRESH_PREFIX + refreshToken);
                }
            }

            String userAccessKey = SecurityConstants.REDIS_USER_ACCESS_PREFIX + userId;
            Set<String> accessTokens = redisTemplate.opsForSet().members(userAccessKey);
            if (accessTokens != null) {
                for (String accessToken : accessTokens) {
                    keys.add(SecurityConstants.REDIS_SESSION_PREFIX + accessToken);
                    keys.add(SecurityConstants.REDIS_ACCESS_REFRESH_BIND_PREFIX + accessToken);
                }
            }

            keys.add(userRefreshKey);
            keys.add(userAccessKey);
            keys.add(SecurityConstants.REDIS_PERMS_PREFIX + userId);
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("[PermissionCache] revoke user sessions failed, userId={}: {}", userId, e.getMessage());
        }
    }
}
