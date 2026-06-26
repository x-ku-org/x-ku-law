package cn.xku.law.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

/** JWT 解析后存放在 SecurityContext 中的当前用户信息 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private String username;
    private Long tenantId;
    /** 权限码集合，从 Redis 权限缓存加载；登录时写入，鉴权时读取 */
    @Builder.Default
    private Set<String> permissions = Collections.emptySet();
}
