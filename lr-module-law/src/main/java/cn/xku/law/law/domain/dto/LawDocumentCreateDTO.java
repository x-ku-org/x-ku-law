package cn.xku.law.law.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** 法规文件新建请求体 */
@Data
public class LawDocumentCreateDTO {

    @NotBlank(message = "法规唯一标识不能为空")
    private String lawUid;

    @NotBlank(message = "法规标题不能为空")
    private String title;

    private String documentNo;

    @NotBlank(message = "法规类型不能为空")
    private String lawType;

    private String legalLevel;
    private String issuingOrg;
    private String regionCode;
    private String industryCode;
    private String subjectDomain;

    @NotNull(message = "时效状态不能为空")
    private String status;

    private LocalDate publishDate;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String officialUrl;
    private String summary;
    private String remark;
}
