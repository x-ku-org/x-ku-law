package cn.xku.law.common.constant;

/** JWT 与 Redis 会话相关常量 */
public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Redis key 前缀：access token → 会话信息 */
    public static final String REDIS_SESSION_PREFIX = "session:access:";

    /** Redis key 前缀：refresh token → 用户ID */
    public static final String REDIS_REFRESH_PREFIX = "session:refresh:";

    /** Redis key 前缀：userId → 该用户当前所有有效 refresh token（Set，支持多端登录） */
    public static final String REDIS_USER_REFRESH_PREFIX = "user:refresh:";

    /** Redis key prefix: userId -> active access tokens for immediate session revocation. */
    public static final String REDIS_USER_ACCESS_PREFIX = "user:access:";

    /** Redis key 前缀：accessToken → 该会话对应的 refresh token（登出时按会话精确撤销） */
    public static final String REDIS_ACCESS_REFRESH_BIND_PREFIX = "session:bind:access:";

    /** Redis key 前缀：userId → 权限码缓存（JSON 数组），鉴权时加载 */
    public static final String REDIS_PERMS_PREFIX = "perms:";

    /** 无登录状态下的占位用户名（用于审计字段填充） */
    public static final String ANONYMOUS_USER = "anonymous";

    /**
     * 默认租户编码。当前对外隐藏租户概念，登录不传 tenantCode 时回落到该值；
     * 将来开放多租户时，登录页恢复租户输入即可。
     */
    public static final String DEFAULT_TENANT_CODE = "platform";

    /** JWT claims 中存储的 key */
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_TOKEN_TYPE = "type";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
}
