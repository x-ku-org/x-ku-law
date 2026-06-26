package cn.xku.law.collect.service;

import cn.xku.law.collect.domain.CollectRecordDO;
import cn.xku.law.collect.domain.RawDocumentDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * 采集接入暂存服务：拥有 lr_collect_record / lr_import_batch / lr_import_record / lr_raw_document
 * 以及 lr_content_source、lr_collect_task 的读写。不依赖法规域或文件服务（仅 lr-common），
 * 跨域编排由 lr-server 的 CollectIngestProcessor / CollectItemPromoter 完成。
 */
public interface CollectIngestService extends IService<RawDocumentDO> {

    /** 按 source_name 解析来源 ID（V5 已种子化），不存在返回 null */
    Long resolveSourceIdByName(String sourceName);

    /** 按 parser_code 解析采集任务 ID（V5 已种子化），不存在返回 null */
    Long resolveTaskIdByParserCode(String parserCode);

    /**
     * 原子地获取或创建运行文件夹处理标记（content_hash=folderKey 作幂等键，配合
     * uk_collect_record_marker 唯一约束）。并发插入冲突时回查同一行返回，保证多实例下唯一标记。
     */
    CollectRecordDO findOrCreateMarker(Long taskId, Long sourceId, String folderKey, String requestUrl);

    /** 原子领取标记：pending→processing，true=领取成功 */
    boolean claimRecord(Long recordId);

    /** 收尾标记记录：写入最终状态/错误/结束时间 */
    void closeCollectRecord(Long recordId, String collectStatus, String errorMessage);

    /** 开启导入批次（status=processing），返回批次 ID */
    Long openImportBatch(String batchNo, Long sourceId);

    /** 收尾导入批次：写入总数/成功/失败/状态/结束时间 */
    void closeImportBatch(Long batchId, int total, int success, int fail, String status);

    /** 插入或按 (sourceId, contentHash) 去重更新原始文档，返回 raw_document ID */
    Long upsertRawDocument(RawDocumentDO draft);

    /** 写一条成功的导入明细 */
    void writeImportRecord(Long batchId, Long sourceId, Long rawDocumentId,
                           Long lawDocumentId, Long lawVersionId,
                           boolean duplicate, Long duplicateDocumentId, Integer rowNo);

    /** 写一条失败的导入明细（独立事务，不随条目回滚而丢失） */
    void writeFailedImportRecord(Long batchId, Long sourceId, Integer rowNo, String error);

    /** 将卡在 processing 且超时的标记重置为 pending 以便下轮重试，返回重置条数 */
    int recoverStuckRecords(LocalDateTime threshold);
}
