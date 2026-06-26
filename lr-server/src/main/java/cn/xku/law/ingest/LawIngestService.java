package cn.xku.law.ingest;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.file.FileService;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.law.service.LawDocumentService;
import cn.xku.law.law.service.LawProcessTaskService;
import cn.xku.law.law.service.LawVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 法规接入共享核心：两条入口（采集批量 {@link CollectItemPromoter} / 管理员上传
 * {@code LawUploadController}）共用。负责「按 lawUid 归并文档 → 按 (docId, versionNo) 挂接草稿版本
 * → 入队处理任务」，把文本提取/分段/发布/解读/变更分析全部交给统一异步管线，保证两路产出一致。
 *
 * <p>本服务只建草稿版本并入队，<b>不</b>在此发布/解析/重算现行版——这些由
 * {@code LawProcessTaskProcessor} 的阶段链完成。整个方法事务内完成，保证 doc/version/入队原子。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LawIngestService {

    private final LawDocumentService lawDocumentService;
    private final LawVersionService lawVersionService;
    private final LawProcessTaskService lawProcessTaskService;
    private final FileService fileService;

    /**
     * 归并文档 + 挂接版本 + 入队处理任务。
     *
     * @param docDto 文档元数据（lawUid 必填，用于归并；已存在则复用，不更新其字段）
     * @param verDto 版本元数据（versionNo 必填；documentId/fileId 由本方法回填）
     * @param fileId 正文文件 ID，可为空（仅元数据接入）
     * @return 归并结果（docId / versionId / 是否重复）
     */
    @Transactional(rollbackFor = Exception.class)
    public LawIngestResult ingest(LawDocumentCreateDTO docDto, LawVersionCreateDTO verDto, Long fileId) {
        fileService.promoteForLawProcessing(fileId);
        Long docId = findOrCreateDocument(docDto);

        LawVersionDO existing = lawVersionService.lambdaQuery()
                .eq(LawVersionDO::getDocumentId, docId)
                .eq(LawVersionDO::getVersionNo, verDto.getVersionNo())
                .one();
        if (existing != null) {
            if (fileId != null && existing.getFileId() == null) {
                existing.setFileId(fileId);
                lawVersionService.updateById(existing);
                lawProcessTaskService.enqueue(docId, existing.getId(), fileId);
            }
            return new LawIngestResult(docId, existing.getId(), true);
        }

        verDto.setDocumentId(docId);
        verDto.setFileId(fileId);
        if (!StringUtils.hasText(verDto.getVersionStatus())) {
            verDto.setVersionStatus("draft");
        }
        if (!StringUtils.hasText(verDto.getRevisionType())) {
            long versionCount = lawVersionService.lambdaQuery()
                    .eq(LawVersionDO::getDocumentId, docId).count();
            verDto.setRevisionType(versionCount == 0 ? "initial" : "revised");
        }
        Long versionId = lawVersionService.createVersion(verDto);
        lawProcessTaskService.enqueue(docId, versionId, fileId);
        return new LawIngestResult(docId, versionId, false);
    }

    /** 按 lawUid 找文档，不存在则建；并发竞态下捕获唯一键冲突后重查。 */
    private Long findOrCreateDocument(LawDocumentCreateDTO docDto) {
        LawDocumentDO existing = lawDocumentService.lambdaQuery()
                .eq(LawDocumentDO::getLawUid, docDto.getLawUid()).one();
        if (existing != null) {
            return existing.getId();
        }
        try {
            return lawDocumentService.createDocument(docDto);
        } catch (AppException e) {
            if (e.getCode() == ErrorCode.LAW_UID_DUPLICATE.getCode()) {
                LawDocumentDO raced = lawDocumentService.lambdaQuery()
                        .eq(LawDocumentDO::getLawUid, docDto.getLawUid()).one();
                if (raced != null) {
                    return raced.getId();
                }
            }
            throw e;
        }
    }
}
