package cn.xku.law.ingest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** 管理员上传单文件接入入参：文件已经过 presign/complete，这里携带表单元数据触发接入。 */
@Data
public class LawUploadIngestDTO {

    @Schema(description = "已上传完成（normal）的文件 ID")
    @NotNull
    private Long fileId;

    @Schema(description = "法规标题")
    @NotBlank
    private String title;

    @Schema(description = "法规稳定唯一标识；留空则按 标题|机关|公布日 生成，便于多版本归并")
    private String lawUid;

    @Schema(description = "版本号；留空则按公布日 yyyyMMdd 生成")
    private String versionNo;

    private String documentNo;
    @Schema(description = "law/regulation/rule/normative/standard/policy")
    private String lawType;
    private String legalLevel;
    private String issuingOrg;
    private String regionCode;
    @Schema(description = "主题领域/分类（如「行政法规」「推荐性国家标准」），用于挂 subject 分类")
    private String subjectDomain;
    @Schema(description = "行业分类编码（如 GB ICS），用于挂 industry 分类")
    private String industryCode;
    @Schema(description = "摘要；留空则由处理管线 AI 富集（仅在为空时写入）")
    private String summary;
    @Schema(description = "标签关键词；与处理管线 AI 标签合并，幂等去重")
    private java.util.List<String> tags;
    @Schema(description = "时效状态：effective/amended/not_effective/expired/repealed/unknown")
    private String status;
    private LocalDate publishDate;
    private LocalDate effectiveDate;
    private String sourceUrl;
}
