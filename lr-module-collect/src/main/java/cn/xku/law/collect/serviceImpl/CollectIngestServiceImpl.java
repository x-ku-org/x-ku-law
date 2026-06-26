package cn.xku.law.collect.serviceImpl;

import cn.xku.law.collect.domain.CollectRecordDO;
import cn.xku.law.collect.domain.CollectTaskDO;
import cn.xku.law.collect.domain.ContentSourceDO;
import cn.xku.law.collect.domain.ImportBatchDO;
import cn.xku.law.collect.domain.ImportRecordDO;
import cn.xku.law.collect.domain.RawDocumentDO;
import cn.xku.law.collect.mapper.CollectRecordMapper;
import cn.xku.law.collect.mapper.CollectTaskMapper;
import cn.xku.law.collect.mapper.ContentSourceMapper;
import cn.xku.law.collect.mapper.ImportBatchMapper;
import cn.xku.law.collect.mapper.ImportRecordMapper;
import cn.xku.law.collect.mapper.RawDocumentMapper;
import cn.xku.law.collect.service.CollectIngestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 采集接入暂存服务实现 */
@Service
@RequiredArgsConstructor
public class CollectIngestServiceImpl extends ServiceImpl<RawDocumentMapper, RawDocumentDO>
        implements CollectIngestService {

    private final ContentSourceMapper contentSourceMapper;
    private final CollectTaskMapper collectTaskMapper;
    private final CollectRecordMapper collectRecordMapper;
    private final ImportBatchMapper importBatchMapper;
    private final ImportRecordMapper importRecordMapper;

    private static String truncate(String s) {
        return s != null && s.length() > 500 ? s.substring(0, 500) : s;
    }

    @Override
    public Long resolveSourceIdByName(String sourceName) {
        ContentSourceDO src = contentSourceMapper.selectOne(new LambdaQueryWrapper<ContentSourceDO>()
                .select(ContentSourceDO::getId)
                .eq(ContentSourceDO::getSourceName, sourceName)
                .last("LIMIT 1"));
        return src != null ? src.getId() : null;
    }

    @Override
    public Long resolveTaskIdByParserCode(String parserCode) {
        CollectTaskDO task = collectTaskMapper.selectOne(new LambdaQueryWrapper<CollectTaskDO>()
                .eq(CollectTaskDO::getParserCode, parserCode)
                .last("LIMIT 1"));
        return task != null ? task.getId() : null;
    }

    @Override
    public CollectRecordDO findOrCreateMarker(Long taskId, Long sourceId, String folderKey, String requestUrl) {
        CollectRecordDO existing = selectMarker(taskId, folderKey);
        if (existing != null) return existing;

        CollectRecordDO record = new CollectRecordDO();
        record.setTaskId(taskId);
        record.setSourceId(sourceId);
        record.setRequestUrl(requestUrl);
        record.setContentHash(folderKey);
        record.setCollectStatus("pending");
        record.setStartedAt(LocalDateTime.now());
        record.setTenantId(0L);
        try {
            collectRecordMapper.insert(record);
            return record;
        } catch (DuplicateKeyException e) {
            // 并发下另一实例已插入同 (task_id, content_hash)，回查返回同一行
            return selectMarker(taskId, folderKey);
        }
    }

    private CollectRecordDO selectMarker(Long taskId, String folderKey) {
        return collectRecordMapper.selectOne(new LambdaQueryWrapper<CollectRecordDO>()
                .eq(CollectRecordDO::getTaskId, taskId)
                .eq(CollectRecordDO::getContentHash, folderKey)
                .last("LIMIT 1"));
    }

    @Override
    public boolean claimRecord(Long recordId) {
        return collectRecordMapper.claimRecord(recordId) == 1;
    }

    @Override
    public void closeCollectRecord(Long recordId, String collectStatus, String errorMessage) {
        CollectRecordDO record = collectRecordMapper.selectById(recordId);
        if (record == null) return;
        record.setCollectStatus(collectStatus);
        record.setErrorMessage(truncate(errorMessage));
        record.setFinishedAt(LocalDateTime.now());
        collectRecordMapper.updateById(record);
    }

    @Override
    public Long openImportBatch(String batchNo, Long sourceId) {
        ImportBatchDO batch = new ImportBatchDO();
        batch.setBatchNo(batchNo);
        batch.setSourceId(sourceId);
        batch.setImportType("collect");
        batch.setStatus("processing");
        batch.setAuditStatus("pending");
        batch.setTotalCount(0);
        batch.setSuccessCount(0);
        batch.setFailCount(0);
        batch.setStartedAt(LocalDateTime.now());
        batch.setTenantId(0L);
        importBatchMapper.insert(batch);
        return batch.getId();
    }

    @Override
    public void closeImportBatch(Long batchId, int total, int success, int fail, String status) {
        ImportBatchDO batch = importBatchMapper.selectById(batchId);
        if (batch == null) return;
        batch.setTotalCount(total);
        batch.setSuccessCount(success);
        batch.setFailCount(fail);
        batch.setStatus(status);
        batch.setFinishedAt(LocalDateTime.now());
        importBatchMapper.updateById(batch);
    }

    @Override
    public Long upsertRawDocument(RawDocumentDO draft) {
        draft.setParseError(truncate(draft.getParseError()));
        if (draft.getContentHash() != null) {
            List<RawDocumentDO> existing = this.lambdaQuery()
                    .eq(RawDocumentDO::getSourceId, draft.getSourceId())
                    .eq(RawDocumentDO::getContentHash, draft.getContentHash())
                    .last("LIMIT 1")
                    .list();
            if (!existing.isEmpty()) {
                draft.setId(existing.get(0).getId());
                this.updateById(draft);
                return draft.getId();
            }
        }
        draft.setTenantId(0L);
        this.save(draft);
        return draft.getId();
    }

    @Override
    public void writeImportRecord(Long batchId, Long sourceId, Long rawDocumentId,
                                  Long lawDocumentId, Long lawVersionId,
                                  boolean duplicate, Long duplicateDocumentId, Integer rowNo) {
        ImportRecordDO record = new ImportRecordDO();
        record.setBatchId(batchId);
        record.setSourceId(sourceId);
        record.setRawDocumentId(rawDocumentId);
        record.setLawDocumentId(lawDocumentId);
        record.setLawVersionId(lawVersionId);
        record.setRecordStatus("success");
        record.setDuplicateFlag(duplicate);
        record.setDuplicateDocumentId(duplicateDocumentId);
        record.setRowNo(rowNo);
        record.setTenantId(0L);
        importRecordMapper.insert(record);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void writeFailedImportRecord(Long batchId, Long sourceId, Integer rowNo, String error) {
        ImportRecordDO record = new ImportRecordDO();
        record.setBatchId(batchId);
        record.setSourceId(sourceId);
        record.setRecordStatus("failed");
        record.setErrorMessage(truncate(error));
        record.setDuplicateFlag(false);
        record.setRowNo(rowNo);
        record.setTenantId(0L);
        importRecordMapper.insert(record);
    }

    @Override
    public int recoverStuckRecords(LocalDateTime threshold) {
        return collectRecordMapper.update(null, new LambdaUpdateWrapper<CollectRecordDO>()
                .eq(CollectRecordDO::getCollectStatus, "processing")
                .lt(CollectRecordDO::getUpdateTime, threshold)
                .set(CollectRecordDO::getCollectStatus, "pending"));
    }
}
