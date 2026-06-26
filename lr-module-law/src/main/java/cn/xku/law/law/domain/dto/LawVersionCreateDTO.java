package cn.xku.law.law.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LawVersionCreateDTO {

    @NotNull
    private Long documentId;
    @NotBlank
    private String versionNo;
    private String versionName;
    private String revisionType;
    private String versionStatus;
    private LocalDate publishDate;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String decisionDocNo;
    private String sourceUrl;
    private Long fileId;
    private String diffSummary;
    private String auditStatus;
}
