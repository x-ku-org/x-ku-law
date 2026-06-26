package cn.xku.law.common.client;

/**
 * 对象存储中单个对象的元信息快照（HEAD/statObject 结果）。
 *
 * @param size        对象字节大小
 * @param etag        对象 ETag（多数实现为内容 MD5，可用于完整性记录）
 * @param contentType 对象 MIME 类型，可能为 null
 */
public record FileStat(long size, String etag, String contentType) {
}
