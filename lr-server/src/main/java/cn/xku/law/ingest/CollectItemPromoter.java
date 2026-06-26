package cn.xku.law.ingest;

import cn.xku.law.collect.domain.RawDocumentDO;
import cn.xku.law.collect.service.CollectIngestService;
import cn.xku.law.collect.source.MappedLaw;
import cn.xku.law.collect.source.SourceAdapter;
import cn.xku.law.collect.source.SourceAdapterRegistry;
import cn.xku.law.file.FileService;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.law.service.LawCategoryAttacher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;

/**
 * 单条采集元数据提升器（源无关编排器）：把一条采集元数据提升为 raw_document + 法规文档/版本 + 导入明细，
 * 全程在一个事务内（{@link Transactional}）。任意失败整条回滚，不留孤儿。
 * 由 {@link CollectIngestProcessor}（非事务）逐条调用，失败由调用方记入失败明细并继续。
 *
 * <p>源专属逻辑（字段映射、文件命名匹配、law_uid/version 规则）下沉到 {@link SourceAdapter}；
 * 「文档归并 + 版本挂接 + 入队处理」复用 {@link LawIngestService}（与管理员上传同一条管线）。
 * 文本提取/分段/发布/解读/变更分析全部由统一异步管线（LawProcessTaskProcessor）完成，本类不再内联解析。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectItemPromoter {

    private final CollectIngestService ingestService;
    private final FileService fileService;
    private final SourceAdapterRegistry sourceAdapterRegistry;
    private final LawIngestService lawIngestService;
    private final LawCategoryAttacher lawCategoryAttacher;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void promote(Map<String, Object> item, String sourceCode, String folder,
                        Set<String> keys, Long sourceId, Long collectRecordId,
                        Long batchId, int rowNo) throws Exception {

        SourceAdapter adapter = sourceAdapterRegistry.resolve(sourceCode);
        if (adapter == null) {
            throw new IllegalStateException("no SourceAdapter registered for source: " + sourceCode);
        }
        MappedLaw m = adapter.map(item);

        Long fileId = null;
        String matchedKey = adapter.matchFileKey(m, folder, keys);
        if (matchedKey != null) {
            String fileName = matchedKey.substring(folder.length());
            fileId = fileService.registerExisting(matchedKey, fileName, "law_version");
        }

        String contentHash = sha256(m.lawUid() + "|" + m.versionKey()
                + "|" + (matchedKey != null ? matchedKey : ""));
        RawDocumentDO raw = new RawDocumentDO();
        raw.setSourceId(sourceId);
        raw.setCollectRecordId(collectRecordId);
        raw.setTitle(m.title());
        raw.setSourceUrl(m.sourceUrl());
        raw.setOriginalDocNo(m.docNo());
        raw.setPublishOrg(m.issuingOrg());
        raw.setPublishDate(m.publishDate());
        raw.setContentHash(contentHash);
        raw.setRawFileId(fileId);
        raw.setParseStatus("metadata_only");
        raw.setMetadataJson(objectMapper.writeValueAsString(item));
        Long rawId = ingestService.upsertRawDocument(raw);

        LawIngestResult result = lawIngestService.ingest(buildDocDTO(m), buildVersionDTO(m), fileId);

        lawCategoryAttacher.attach(result.documentId(), "region", m.regionCode());
        lawCategoryAttacher.attach(result.documentId(), "subject", m.subjectDomain());
        lawCategoryAttacher.attach(result.documentId(), "industry", m.industryCode());

        ingestService.writeImportRecord(batchId, sourceId, rawId, result.documentId(), result.versionId(),
                result.duplicate(), result.duplicate() ? result.documentId() : null, rowNo);
    }

    private LawDocumentCreateDTO buildDocDTO(MappedLaw m) {
        LawDocumentCreateDTO dto = new LawDocumentCreateDTO();
        dto.setLawUid(m.lawUid());
        dto.setTitle(m.title());
        dto.setDocumentNo(m.docNo());
        dto.setLawType(m.lawType());
        dto.setLegalLevel(m.legalLevel());
        dto.setIssuingOrg(m.issuingOrg());
        dto.setRegionCode(m.regionCode());
        dto.setSubjectDomain(m.subjectDomain());
        dto.setIndustryCode(m.industryCode());
        dto.setStatus(m.status());
        dto.setPublishDate(m.publishDate());
        dto.setEffectiveDate(m.effectiveDate());
        dto.setOfficialUrl(m.sourceUrl());
        return dto;
    }

    private LawVersionCreateDTO buildVersionDTO(MappedLaw m) {
        LawVersionCreateDTO dto = new LawVersionCreateDTO();
        dto.setVersionNo(m.versionKey());
        dto.setVersionName(m.title());
        dto.setVersionStatus("draft");
        dto.setPublishDate(m.publishDate());
        dto.setEffectiveDate(m.effectiveDate());
        dto.setSourceUrl(m.sourceUrl());
        return dto;
    }

    private static String sha256(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
