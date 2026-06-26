package cn.xku.law.auth;

import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.security.LoginUser;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.security.JwtTokenUtil;
import cn.xku.law.system.domain.LoginLogDO;
import cn.xku.law.system.domain.TenantDO;
import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.mapper.LoginLogMapper;
import cn.xku.law.system.mapper.TenantMapper;
import cn.xku.law.system.mapper.UserMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final AvatarUrls avatarUrls;

    @Override
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        // 对外隐藏租户：未传 tenantCode 时回落到默认租户 platform。
        String tenantCode = StringUtils.hasText(request.tenantCode())
                ? request.tenantCode() : SecurityConstants.DEFAULT_TENANT_CODE;
        TenantDO tenant = tenantMapper.selectOne(
                new LambdaQueryWrapper<TenantDO>().eq(TenantDO::getTenantCode, tenantCode));
        if (tenant == null) {
            writeLoginLog(null, request.username(), 0L, "fail", ip, userAgent, "租户不存在");
            throw new AppException(ErrorCode.AUTH_TENANT_NOT_FOUND);
        }
        if (!"enabled".equals(tenant.getStatus())) {
            writeLoginLog(null, request.username(), tenant.getId(), "fail", ip, userAgent, "租户已禁用");
            throw new AppException(ErrorCode.AUTH_TENANT_DISABLED);
        }

        Long tenantId = tenant.getId();

        UserDO user = userMapper.selectByUsernameAndTenantId(request.username(), tenantId);
        if (user == null) {
            writeLoginLog(null, request.username(), tenantId, "fail", ip, userAgent, "用户不存在");
            throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        if (!"enabled".equals(user.getStatus())) {
            writeLoginLog(user.getId(), user.getUsername(), tenantId, "fail", ip, userAgent, "用户已被禁用");
            throw new AppException(ErrorCode.AUTH_USER_DISABLED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            writeLoginLog(user.getId(), user.getUsername(), tenantId, "fail", ip, userAgent, "密码错误");
            throw new AppException(ErrorCode.AUTH_PASSWORD_ERROR);
        }

        Set<String> permissions = loadPermissionCodes(user.getId(), tenantId);

        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .tenantId(tenantId)
                .permissions(permissions)
                .build();

        String accessToken = jwtTokenUtil.generateAccessToken(loginUser);
        String refreshToken = jwtTokenUtil.generateRefreshToken(loginUser);

        long accessTtl = jwtTokenUtil.getAccessTtlSeconds();
        long refreshTtl = jwtTokenUtil.getRefreshTtlSeconds();
        cacheAccessSession(accessToken, user.getId(), accessTtl);
        redisTemplate.opsForValue().set(
                SecurityConstants.REDIS_REFRESH_PREFIX + refreshToken,
                String.valueOf(user.getId()),
                refreshTtl,
                TimeUnit.SECONDS);
        // userId → 多端 refresh token 集合（支持多端登录；登出按会话精确撤销其中一个）
        String userRefreshKey = SecurityConstants.REDIS_USER_REFRESH_PREFIX + user.getId();
        redisTemplate.opsForSet().add(userRefreshKey, refreshToken);
        redisTemplate.expire(userRefreshKey, refreshTtl, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(
                SecurityConstants.REDIS_ACCESS_REFRESH_BIND_PREFIX + accessToken, refreshToken, accessTtl, TimeUnit.SECONDS);

        cachePermissions(user.getId(), permissions);

        writeLoginLog(user.getId(), user.getUsername(), tenantId, "success", ip, userAgent, null);

        return new LoginResponse(accessToken, refreshToken, jwtTokenUtil.getAccessTtlSeconds());
    }

    @Override
    public LoginResponse register(RegisterRequest request, String ip, String userAgent) {
        // 自助注册统一落默认租户 platform（对外隐藏多租户）。
        TenantDO tenant = tenantMapper.selectOne(
                new LambdaQueryWrapper<TenantDO>().eq(TenantDO::getTenantCode, SecurityConstants.DEFAULT_TENANT_CODE));
        if (tenant == null) {
            throw new AppException(ErrorCode.AUTH_TENANT_NOT_FOUND);
        }
        if (!"enabled".equals(tenant.getStatus())) {
            throw new AppException(ErrorCode.AUTH_TENANT_DISABLED);
        }
        Long tenantId = tenant.getId();

        // 唯一性预校验（按租户作用域，绕过租户插件的显式查询）。
        if (userMapper.selectByUsernameAndTenantId(request.username(), tenantId) != null) {
            throw new AppException(ErrorCode.AUTH_USERNAME_EXISTS);
        }
        if (userMapper.selectByMobileAndTenantId(request.mobile(), tenantId) != null) {
            throw new AppException(ErrorCode.AUTH_MOBILE_EXISTS);
        }

        UserDO user = new UserDO();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setMobile(request.mobile());
        user.setNickname(request.username());
        user.setUserType("normal");
        user.setStatus("enabled");
        user.setPasswordUpdateTime(LocalDateTime.now());
        // 显式置租户：未登录态下租户插件会注入 0，显式提供该列可让插件跳过注入，保证落到 platform 租户。
        user.setTenantId(tenantId);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发或软删除残留触发 DB 唯一键冲突时，按冲突列回退为友好错误码。
            String msg = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase();
            if (msg.contains("mobile")) {
                throw new AppException(ErrorCode.AUTH_MOBILE_EXISTS);
            }
            throw new AppException(ErrorCode.AUTH_USERNAME_EXISTS);
        }

        // 复用登录链路签发令牌、写登录日志，避免重复实现会话管理。
        return login(new LoginRequest(tenant.getTenantCode(), request.username(), request.password()), ip, userAgent);
    }

    @Override
    public String refresh(String refreshToken) {
        LoginUser loginUser = jwtTokenUtil.parseToken(refreshToken);
        if (loginUser == null) {
            throw new AppException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        String refreshKey = SecurityConstants.REDIS_REFRESH_PREFIX + refreshToken;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey))) {
            throw new AppException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        long accessTtl = jwtTokenUtil.getAccessTtlSeconds();
        UserDO activeUser = validateRefreshPrincipal(loginUser, refreshToken);
        Long tenantId = activeUser.getTenantId();
        Set<String> permissions = loadPermissionCodes(activeUser.getId(), tenantId);
        LoginUser refreshedUser = LoginUser.builder()
                .userId(activeUser.getId())
                .username(activeUser.getUsername())
                .tenantId(tenantId)
                .permissions(permissions)
                .build();
        String newAccessToken = jwtTokenUtil.generateAccessToken(refreshedUser);
        cacheAccessSession(newAccessToken, activeUser.getId(), accessTtl);
        // 绑定新 access 会话 → 同一 refresh token，登出仍能精确撤销本会话
        redisTemplate.opsForValue().set(
                SecurityConstants.REDIS_ACCESS_REFRESH_BIND_PREFIX + newAccessToken,
                refreshToken, accessTtl, TimeUnit.SECONDS);

        // 续期 refresh TTL，并保持 userId → refresh token 集合与单 token TTL 同步。
        long refreshTtl = jwtTokenUtil.getRefreshTtlSeconds();
        redisTemplate.expire(refreshKey, refreshTtl, TimeUnit.SECONDS);
        String userRefreshKey = SecurityConstants.REDIS_USER_REFRESH_PREFIX + activeUser.getId();
        redisTemplate.opsForSet().add(userRefreshKey, refreshToken);
        redisTemplate.expire(userRefreshKey, refreshTtl, TimeUnit.SECONDS);

        //    loadPermissions 取空而沦为“零权限令牌”，导致所有 @PreAuthorize 接口 403。
        cachePermissions(activeUser.getId(), permissions);

        return newAccessToken;
    }

    @Override
    public void logout(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return;
        }
        LoginUser loginUser = jwtTokenUtil.parseToken(accessToken);

        redisTemplate.delete(SecurityConstants.REDIS_SESSION_PREFIX + accessToken);

        // 取出本会话绑定的 refresh token，仅撤销这一端（不影响其他端登录）
        String bindKey = SecurityConstants.REDIS_ACCESS_REFRESH_BIND_PREFIX + accessToken;
        String sessionRefreshToken = redisTemplate.opsForValue().get(bindKey);
        redisTemplate.delete(bindKey);

        if (loginUser != null) {
            Long userId = loginUser.getUserId();
            redisTemplate.opsForSet().remove(SecurityConstants.REDIS_USER_ACCESS_PREFIX + userId, accessToken);
            String userRefreshKey = SecurityConstants.REDIS_USER_REFRESH_PREFIX + userId;
            if (StringUtils.hasText(sessionRefreshToken)) {
                redisTemplate.delete(SecurityConstants.REDIS_REFRESH_PREFIX + sessionRefreshToken);
                redisTemplate.opsForSet().remove(userRefreshKey, sessionRefreshToken);
            }
            Long remaining = redisTemplate.opsForSet().size(userRefreshKey);
            if (remaining == null || remaining == 0L) {
                redisTemplate.delete(SecurityConstants.REDIS_PERMS_PREFIX + userId);
            }
        }
    }

    @Override
    public CurrentUserVO currentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = loginUser.getUserId();
        Long tenantId = loginUser.getTenantId();

        UserDO user = userMapper.selectById(userId);
        TenantDO tenant = tenantMapper.selectById(tenantId);

        List<String> roles;
        try {
            roles = userRoleMapper.selectRoleCodes(userId, tenantId);
        } catch (Exception e) {
            log.warn("[AuthService] 加载角色码失败，降级为空角色: userId={}", userId, e);
            roles = List.of();
        }
        if (roles == null) roles = List.of();

        List<String> permissions = loginUser.getPermissions() == null
                ? List.of()
                : new ArrayList<>(loginUser.getPermissions());

        return new CurrentUserVO(
                userId,
                loginUser.getUsername(),
                user != null ? user.getRealName() : null,
                user != null ? user.getUserType() : null,
                avatarUrls.publicPath(user),
                tenant != null ? tenant.getTenantCode() : null,
                tenant != null ? tenant.getTenantName() : null,
                roles,
                permissions
        );
    }

    /** 加载用户权限码；失败降级为空集合，不阻断登录/刷新主流程。 */
    private Set<String> loadPermissionCodes(Long userId, Long tenantId) {
        Set<String> permissions = new HashSet<>();
        try {
            List<String> codes = userRoleMapper.selectPermissionCodes(userId, tenantId);
            if (codes != null) permissions.addAll(codes);
        } catch (Exception e) {
            log.warn("[AuthService] 加载权限码失败，降级为空权限: userId={}", userId, e);
        }
        return permissions;
    }

    private UserDO validateRefreshPrincipal(LoginUser loginUser, String refreshToken) {
        UserDO user = userMapper.selectActiveByIdIgnoreTenant(loginUser.getUserId());
        if (user == null || user.getTenantId() == null || !user.getTenantId().equals(loginUser.getTenantId())) {
            revokeRefreshSession(refreshToken, loginUser.getUserId());
            throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        if (!"enabled".equals(user.getStatus())) {
            revokeRefreshSession(refreshToken, user.getId());
            throw new AppException(ErrorCode.AUTH_USER_DISABLED);
        }

        TenantDO tenant = tenantMapper.selectById(user.getTenantId());
        if (tenant == null) {
            revokeRefreshSession(refreshToken, user.getId());
            throw new AppException(ErrorCode.AUTH_TENANT_NOT_FOUND);
        }
        if (!"enabled".equals(tenant.getStatus())) {
            revokeRefreshSession(refreshToken, user.getId());
            throw new AppException(ErrorCode.AUTH_TENANT_DISABLED);
        }
        return user;
    }

    private void revokeRefreshSession(String refreshToken, Long userId) {
        redisTemplate.delete(SecurityConstants.REDIS_REFRESH_PREFIX + refreshToken);
        if (userId != null) {
            redisTemplate.opsForSet().remove(SecurityConstants.REDIS_USER_REFRESH_PREFIX + userId, refreshToken);
            redisTemplate.delete(SecurityConstants.REDIS_PERMS_PREFIX + userId);
        }
    }

    private void cacheAccessSession(String accessToken, Long userId, long accessTtl) {
        redisTemplate.opsForValue().set(
                SecurityConstants.REDIS_SESSION_PREFIX + accessToken,
                String.valueOf(userId),
                accessTtl,
                TimeUnit.SECONDS);
        String userAccessKey = SecurityConstants.REDIS_USER_ACCESS_PREFIX + userId;
        redisTemplate.opsForSet().add(userAccessKey, accessToken);
        redisTemplate.expire(userAccessKey, accessTtl, TimeUnit.SECONDS);
    }

    /** 写权限缓存，TTL 与 access token 对齐；序列化失败时写入空数组占位。 */
    private void cachePermissions(Long userId, Set<String> permissions) {
        String key = SecurityConstants.REDIS_PERMS_PREFIX + userId;
        long ttl = jwtTokenUtil.getAccessTtlSeconds();
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(permissions), ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[AuthService] 序列化权限缓存失败: {}", e.getMessage());
            redisTemplate.opsForValue().set(key, "[]", ttl, TimeUnit.SECONDS);
        }
    }

    private void writeLoginLog(Long userId, String username, Long tenantId,
                               String status, String ip, String userAgent, String failReason) {
        try {
            LoginLogDO logDO = new LoginLogDO();
            logDO.setUserId(userId);
            logDO.setUsername(username);
            logDO.setTenantId(tenantId);
            logDO.setLoginType("password");
            logDO.setLoginStatus(status);
            logDO.setIp(ip);
            logDO.setUserAgent(userAgent);
            logDO.setLoginTime(LocalDateTime.now());
            logDO.setFailReason(failReason);
            loginLogMapper.insert(logDO);
        } catch (Exception e) {
            log.warn("[AuthService] 写入登录日志失败，不影响登录流程: {}", e.getMessage());
        }
    }
}
