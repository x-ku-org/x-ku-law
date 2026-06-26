package cn.xku.law.file;

import cn.xku.law.common.client.FileStat;
import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.config.StorageProperties;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基于 MinIO Java SDK 的 {@link FileStorageClient} 实现（同时兼容 S3 / 阿里云 OSS 的 S3 接口）。
 * <p>bean 名 {@code ossFileStorageClient}，仅在配置了存储 endpoint 时启用。
 */
@Slf4j
@Primary
@Component("ossFileStorageClient")
@ConditionalOnExpression("'${app.storage.endpoint:}' != ''")
@RequiredArgsConstructor
public class MinioFileStorageClient implements FileStorageClient {

    private final StorageProperties props;
    private MinioClient client;

    @PostConstruct
    void init() {
        this.client = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
        log.info("[MinioFileStorageClient] initialized. endpoint={}, bucket={}",
                props.getEndpoint(), props.getBucket());
    }

    @Override
    public String upload(String objectKey, byte[] data, String contentType) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .stream(in, data.length, -1)
                    .contentType(contentType)
                    .build());
            return objectKey;
        } catch (Exception e) {
            throw fail("upload", objectKey, e);
        }
    }

    @Override
    public byte[] download(String objectKey) {
        try (GetObjectResponse resp = client.getObject(GetObjectArgs.builder()
                .bucket(props.getBucket())
                .object(objectKey)
                .build())) {
            return resp.readAllBytes();
        } catch (Exception e) {
            throw fail("download", objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw fail("delete", objectKey, e);
        }
    }

    @Override
    public String getAccessUrl(String objectKey) {
        if (props.isPublicRead()) {
            return trimTrailingSlash(props.getEndpoint()) + "/" + props.getBucket() + "/" + objectKey;
        }
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .expiry(props.getPresignExpireSeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw fail("getAccessUrl", objectKey, e);
        }
    }

    @Override
    public String generatePresignedPutUrl(String objectKey, String contentType, int expirySeconds) {
        try {
            GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .expiry(expirySeconds, TimeUnit.SECONDS);
            // 将 Content-Type 纳入签名:前端 PUT 时必须带相同 Content-Type,否则 MinIO 返回 403,
            if (contentType != null && !contentType.isBlank()) {
                builder.extraHeaders(Map.of("Content-Type", contentType));
            }
            return client.getPresignedObjectUrl(builder.build());
        } catch (Exception e) {
            throw fail("generatePresignedPutUrl", objectKey, e);
        }
    }

    @Override
    public FileStat statObject(String objectKey) {
        try {
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .build());
            return new FileStat(stat.size(), stat.etag(), stat.contentType());
        } catch (ErrorResponseException e) {
            // 对象不存在（NoSuchKey）视为未上传，返回 null
            String code = e.errorResponse() != null ? e.errorResponse().code() : null;
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code)) {
                return null;
            }
            throw fail("statObject", objectKey, e);
        } catch (Exception e) {
            throw fail("statObject", objectKey, e);
        }
    }

    @Override
    public List<String> list(String prefix) {
        try {
            Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                    .bucket(props.getBucket())
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            List<String> keys = new ArrayList<>();
            for (Result<Item> result : results) {
                keys.add(result.get().objectName());
            }
            return keys;
        } catch (Exception e) {
            throw fail("list", prefix, e);
        }
    }

    private AppException fail(String op, String objectKey, Exception e) {
        log.error("[MinioFileStorageClient] {} failed. key={}", op, objectKey, e);
        return new AppException(ErrorCode.SYS_FILE_UPLOAD_FAIL,
                "对象存储操作失败：" + op + " " + objectKey);
    }

    private static String trimTrailingSlash(String s) {
        return s != null && s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
