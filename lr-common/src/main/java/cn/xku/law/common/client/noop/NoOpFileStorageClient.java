package cn.xku.law.common.client.noop;

import cn.xku.law.common.client.FileStat;
import cn.xku.law.common.client.FileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.List;

/** FileStorageClient 空实现，OSS 未接入时自动激活。bean 名 {@code noOpFileStorageClient}。 */
@Slf4j
@Component("noOpFileStorageClient")
@ConditionalOnExpression("'${app.storage.endpoint:}' == ''")
public class NoOpFileStorageClient implements FileStorageClient {

    @Override
    public String upload(String objectKey, byte[] data, String contentType) {
        log.warn("[NoOpFileStorageClient] upload called — storage not configured. key={}", objectKey);
        return objectKey;
    }

    @Override
    public byte[] download(String objectKey) {
        log.warn("[NoOpFileStorageClient] download called — storage not configured. key={}", objectKey);
        return new byte[0];
    }

    @Override
    public void delete(String objectKey) {
        log.warn("[NoOpFileStorageClient] delete called — storage not configured. key={}", objectKey);
    }

    @Override
    public String getAccessUrl(String objectKey) {
        log.warn("[NoOpFileStorageClient] getAccessUrl called — storage not configured. key={}", objectKey);
        return "";
    }

    @Override
    public String generatePresignedPutUrl(String objectKey, String contentType, int expirySeconds) {
        log.warn("[NoOpFileStorageClient] generatePresignedPutUrl called — storage not configured. key={}", objectKey);
        return "";
    }

    @Override
    public FileStat statObject(String objectKey) {
        log.warn("[NoOpFileStorageClient] statObject called — storage not configured. key={}", objectKey);
        return null;
    }

    @Override
    public List<String> list(String prefix) {
        log.warn("[NoOpFileStorageClient] list called — storage not configured. prefix={}", prefix);
        return List.of();
    }
}
