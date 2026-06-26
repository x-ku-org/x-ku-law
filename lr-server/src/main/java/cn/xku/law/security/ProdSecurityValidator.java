package cn.xku.law.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** prod profile 启动时校验安全配置，防止使用开发默认值上线 */
@Slf4j
@Component
@Profile("prod")
public class ProdSecurityValidator implements InitializingBean {

    private static final String DEV_DEFAULT_SECRET =
            "bXktc2VjcmV0LWtleS1tdXN0LWJlLWF0LWxlYXN0LTI1Ni1iaXRzLWxvbmcK";

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Override
    public void afterPropertiesSet() {
        if (!StringUtils.hasText(jwtSecret) || DEV_DEFAULT_SECRET.equals(jwtSecret.trim())) {
            throw new IllegalStateException(
                    "[Security] JWT_SECRET 使用了开发默认值，生产环境禁止启动。" +
                    "请通过环境变量 JWT_SECRET 注入随机生成的 256-bit Base64 密钥（openssl rand -base64 32）。");
        }
        log.info("[Security] JWT_SECRET validation passed");
    }
}
