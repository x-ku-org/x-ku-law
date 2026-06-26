package cn.xku.law.common.client;

import java.util.List;

/**
 * 对象存储客户端接口（OSS / MinIO / S3 统一抽象）。
 * 具体实现（MinIO/OSS）由 lr-server 提供，bean 名为 {@code ossFileStorageClient}。
 */
public interface FileStorageClient {

    /**
     * 上传文件，返回存储 key。
     *
     * @param objectKey   存储路径（含文件名），如 "law/pdf/2024/doc123.pdf"
     * @param data        文件内容
     * @param contentType MIME 类型
     * @return 存储 key（与 lr_file_object.object_key 对应）
     */
    String upload(String objectKey, byte[] data, String contentType);

    /** 下载文件内容 */
    byte[] download(String objectKey);

    /** 删除文件 */
    void delete(String objectKey);

    /** 获取可访问 URL（私有桶返回临时签名 GET URL，公开桶返回固定 URL） */
    String getAccessUrl(String objectKey);

    /**
     * 生成用于前端直传的预签名 PUT URL。前端需以该 URL 发起 PUT，
     * 并带上与签名一致的 {@code Content-Type} 请求头。
     *
     * @param objectKey      预分配的存储 key
     * @param contentType    上传时使用的 MIME 类型
     * @param expirySeconds  URL 有效期（秒）
     * @return 预签名 PUT URL
     */
    String generatePresignedPutUrl(String objectKey, String contentType, int expirySeconds);

    /**
     * 读取对象元信息（HEAD）。用于前端直传完成后校验对象确已写入。
     *
     * @param objectKey 存储 key
     * @return 对象元信息；对象不存在时返回 {@code null}
     */
    FileStat statObject(String objectKey);

    /**
     * 列出指定前缀下的所有对象 key（递归）。用于扫描采集产物的运行文件夹。
     *
     * @param prefix 对象 key 前缀，如 "fglaw"
     * @return 匹配的对象 key 列表；无匹配时返回空列表
     */
    List<String> list(String prefix);
}
