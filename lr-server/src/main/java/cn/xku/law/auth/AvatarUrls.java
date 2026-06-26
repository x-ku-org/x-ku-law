package cn.xku.law.auth;

import cn.xku.law.system.domain.UserDO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.util.Base64;

/**
 * 头像对前端的可用地址：私有桶下不能直接存预签名 URL（会过期），
 * 故 lr_user.avatar_url 存对象 key，前端统一经 GET /account/avatar/{userId} 取流。
 * 这里把它拼成带版本号的稳定相对路径（?v 随 update_time 变化，用于上传后破缓存）。
 *
 * <p>取流端点对外公开（供 &lt;img&gt; 直接展示、无法携带鉴权头），因此 URL 额外带一段
 * 按 userId 计算的 HMAC 签名 {@code &t=}：取流时校验签名由本服务签发，杜绝按自增 id
 * 顺序枚举、跨租户拉取他人头像。签名只绑定 userId（不绑定 ?v），故头像更新后旧链接仍可用。
 */
@Component
public class AvatarUrls {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final byte[] signingKey;

    public AvatarUrls(@Value("${app.security.jwt.secret}") String secret) {
        this.signingKey = secret.getBytes(StandardCharsets.UTF_8);
    }

    /** 用户已设头像时返回 {@code /account/avatar/{id}?v=epoch&t=sig}，否则返回 null（前端回退默认头像）。 */
    public String publicPath(UserDO user) {
        if (user == null || user.getId() == null || !StringUtils.hasText(user.getAvatarUrl())) {
            return null;
        }
        long version = user.getUpdateTime() != null ? user.getUpdateTime().toEpochSecond(ZoneOffset.UTC) : 0L;
        return "/account/avatar/" + user.getId() + "?v=" + version + "&t=" + sign(user.getId());
    }

    /** 校验取流请求携带的签名是否由本服务签发（防枚举）；常量时间比较，失败一律按未授权处理。 */
    public boolean verify(Long userId, String token) {
        if (userId == null || !StringUtils.hasText(token)) {
            return false;
        }
        byte[] expected = sign(userId).getBytes(StandardCharsets.UTF_8);
        byte[] actual = token.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    /** HMAC-SHA256(secret, "avatar:"+userId) → base64url（无填充），稳定且不可被无密钥方伪造。 */
    private String sign(long userId) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(signingKey, HMAC_ALGO));
            byte[] raw = mac.doFinal(("avatar:" + userId).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("avatar signature failed", e);
        }
    }
}
