package cn.xku.law.security;

import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.security.LoginUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 鉴权过滤器：提取 Bearer token → 解析 JWT → 校验 Redis 会话 → 加载权限 → 写入 SecurityContext。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            LoginUser loginUser = jwtTokenUtil.parseToken(token);
            if (loginUser != null && isSessionActive(token)) {
                try {
                    Set<SimpleGrantedAuthority> authorities = loadPermissions(loginUser.getUserId());
                    if (authorities != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    log.warn("[JwtAuthFilter] Redis 不可用，fail-closed，拒绝认证: uri={}, err={}",
                            request.getRequestURI(), e.getMessage());
                }
            } else {
                log.debug("[JwtAuthFilter] invalid or expired session, uri={}", request.getRequestURI());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return header.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /** Redis 不可用时返回 false（fail-closed），避免抛 500 */
    private boolean isSessionActive(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(SecurityConstants.REDIS_SESSION_PREFIX + token));
        } catch (Exception e) {
            log.warn("[JwtAuthFilter] Redis 不可用，session 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Redis 权限缓存加载权限码。Redis 故障时抛出异常，由调用方决策（fail-closed）。
     * 权限键不存在（首次登录/缓存过期）返回空集合；这与 Redis 宕机是不同的语义。
     */
    private Set<SimpleGrantedAuthority> loadPermissions(Long userId) {
        String json = redisTemplate.opsForValue().get(SecurityConstants.REDIS_PERMS_PREFIX + userId);
        if (!StringUtils.hasText(json)) {
            return null;
        }
        if ("[]".equals(json)) {
            return Collections.emptySet();
        }
        try {
            List<String> codes = objectMapper.readValue(json, new TypeReference<>() {});
            return codes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("[JwtAuthFilter] 权限缓存反序列化失败: userId={}, err={}", userId, e.getMessage());
            return Collections.emptySet();
        }
    }
}
