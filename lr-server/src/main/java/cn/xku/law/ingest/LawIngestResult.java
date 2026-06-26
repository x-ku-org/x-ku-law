package cn.xku.law.ingest;

/**
 * 一次接入的结果：归并到的文档 ID、版本 ID，以及该版本是否为重复（已存在，未新建）。
 */
public record LawIngestResult(Long documentId, Long versionId, boolean duplicate) {
}
