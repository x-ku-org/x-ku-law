package cn.xku.law.ingest;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.ingest.dto.LawUploadIngestDTO;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.process.LawProcessTaskProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;

/**
 * 管理员单文件上传接入 API。文件先经 /files/presign + /{id}/complete 上传到对象存储，
 * 再调本接口携带表单元数据触发接入：归并文档 + 建草稿版本 + 入队统一处理管线
 * （与采集批量同一条管线，产出一致）。需平台管理员（tenant_id=0）操作。
 */
@Tag(name = "法规接入", description = "管理员上传单个法规文件并触发处理管线")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/ingest")
@RequiredArgsConstructor
public class LawUploadController {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LawIngestService lawIngestService;
    private final cn.xku.law.law.service.LawCategoryAttacher lawCategoryAttacher;
    private final cn.xku.law.law.service.LawTagAttacher lawTagAttacher;
    /** 处理管线消费者：仅 app.process.enabled=true 时存在，用于上传后立刻触发处理。 */
    private final ObjectProvider<LawProcessTaskProcessor> processTaskProcessorProvider;

    @Operation(summary = "上传法规文件并接入",
            description = "传入已上传完成的 fileId + 元数据；系统建版本并入队，由处理管线异步完成提取/分段/发布/解读/变更分析。返回版本 ID。")
    @PreAuthorize("hasAuthority('law:ingest:upload')")
    @OperLog(module = "法规接入", type = "upload")
    @PostMapping("/upload")
    public CommonResult<Long> upload(@Valid @RequestBody LawUploadIngestDTO dto) {
        LawDocumentCreateDTO docDto = new LawDocumentCreateDTO();
        docDto.setLawUid(resolveLawUid(dto));
        docDto.setTitle(dto.getTitle());
        docDto.setDocumentNo(dto.getDocumentNo());
        docDto.setLawType(StringUtils.hasText(dto.getLawType()) ? dto.getLawType() : "regulation");
        docDto.setLegalLevel(dto.getLegalLevel());
        docDto.setIssuingOrg(dto.getIssuingOrg());
        docDto.setRegionCode(dto.getRegionCode());
        docDto.setSubjectDomain(dto.getSubjectDomain());
        docDto.setIndustryCode(dto.getIndustryCode());
        docDto.setSummary(dto.getSummary());
        docDto.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "effective");
        docDto.setPublishDate(dto.getPublishDate());
        docDto.setEffectiveDate(dto.getEffectiveDate());
        docDto.setOfficialUrl(dto.getSourceUrl());

        LawVersionCreateDTO verDto = new LawVersionCreateDTO();
        verDto.setVersionNo(resolveVersionNo(dto));
        verDto.setVersionName(dto.getTitle());
        verDto.setVersionStatus("draft");
        verDto.setPublishDate(dto.getPublishDate());
        verDto.setEffectiveDate(dto.getEffectiveDate());
        verDto.setSourceUrl(dto.getSourceUrl());

        LawIngestResult result = lawIngestService.ingest(docDto, verDto, dto.getFileId());
        // 与采集路径一致：填了的维度挂分类（幂等，空值内部忽略）。
        lawCategoryAttacher.attach(result.documentId(), "region", dto.getRegionCode());
        lawCategoryAttacher.attach(result.documentId(), "subject", dto.getSubjectDomain());
        lawCategoryAttacher.attach(result.documentId(), "industry", dto.getIndustryCode());
        if (dto.getTags() != null) {
            dto.getTags().forEach(tag -> lawTagAttacher.attach(result.documentId(), tag));
        }
        processTaskProcessorProvider.ifAvailable(p -> p.triggerNow(result.versionId()));
        return CommonResult.success(result.versionId());
    }

    private String resolveLawUid(LawUploadIngestDTO dto) {
        if (StringUtils.hasText(dto.getLawUid())) {
            return dto.getLawUid();
        }
        String basis = dto.getTitle() + "|" + nullToEmpty(dto.getIssuingOrg())
                + "|" + (dto.getPublishDate() != null ? dto.getPublishDate() : "");
        return "UPLOAD:" + sha256(basis);
    }

    private String resolveVersionNo(LawUploadIngestDTO dto) {
        if (StringUtils.hasText(dto.getVersionNo())) {
            return dto.getVersionNo();
        }
        return dto.getPublishDate() != null ? dto.getPublishDate().format(YYYYMMDD) : "1";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
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
