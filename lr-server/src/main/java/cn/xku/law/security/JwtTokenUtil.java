package cn.xku.law.security;

import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.security.LoginUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/** JWT 生成与解析工具，密钥与 TTL 从 application.yml 读取 */
@Slf4j
@Component
public class JwtTokenUtil {

    private final SecretKey secretKey;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtTokenUtil(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-ttl-seconds:7200}") long accessTtlSeconds,
            @Value("${app.security.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTtlMs = accessTtlSeconds * 1000;
        this.refreshTtlMs = refreshTtlSeconds * 1000;
    }

    public String generateAccessToken(LoginUser user) {
        return buildToken(user, SecurityConstants.TOKEN_TYPE_ACCESS, accessTtlMs);
    }

    public String generateRefreshToken(LoginUser user) {
        return buildToken(user, SecurityConstants.TOKEN_TYPE_REFRESH, refreshTtlMs);
    }

    private String buildToken(LoginUser user, String tokenType, long ttlMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim(SecurityConstants.CLAIM_USER_ID, user.getUserId())
                .claim(SecurityConstants.CLAIM_USERNAME, user.getUsername())
                .claim(SecurityConstants.CLAIM_TENANT_ID, user.getTenantId())
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, tokenType)
                // jti：保证每次签发的 token 唯一。JWT 的 iat/exp 仅精确到秒，
                // 同一用户同一秒内的多端登录否则会生成完全相同的 token，
                // 导致多端会话坍缩（共用一个 Redis 会话键），破坏 P1-5 的按会话精确撤销。
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT，返回 LoginUser；token 无效或过期时返回 null。
     */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return LoginUser.builder()
                    .userId(claims.get(SecurityConstants.CLAIM_USER_ID, Long.class))
                    .username(claims.get(SecurityConstants.CLAIM_USERNAME, String.class))
                    .tenantId(claims.get(SecurityConstants.CLAIM_TENANT_ID, Long.class))
                    .build();
        } catch (ExpiredJwtException e) {
            log.debug("[JwtTokenUtil] token expired");
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[JwtTokenUtil] token invalid: {}", e.getMessage());
            return null;
        }
    }

    public long getAccessTtlSeconds() {
        return accessTtlMs / 1000;
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlMs / 1000;
    }
}
