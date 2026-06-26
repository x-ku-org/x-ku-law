package cn.xku.law.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** 对象存储配置，对应 application.yml 中 {@code app.storage.*}。 */
@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** 服务端点，如 MinIO http://host:9000 或 OSS 域名 */
    private String endpoint;

    /** 桶名称 */
    private String bucket;

    /** 访问凭证 AccessKey */
    private String accessKey;

    /** 访问凭证 SecretKey */
    private String secretKey;

    /** 预签名 URL 有效期（秒），默认 10 分钟 */
    private int presignExpireSeconds = 600;

    /** 单文件大小上限（字节），默认 50MB */
    private long maxFileSize = 50L * 1024 * 1024;

    /** 是否公开读桶：true 则 getAccessUrl 返回固定 URL，false 返回临时预签名 GET URL */
    private boolean publicRead = false;

    /** 允许上传的 MIME 类型白名单；为空表示不限制 */
    private List<String> allowedContentTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
}
