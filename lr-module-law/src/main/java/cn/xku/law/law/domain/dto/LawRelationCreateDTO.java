package cn.xku.law.law.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LawRelationCreateDTO {

    @NotNull
    private Long sourceDocumentId;
    private Long sourceVersionId;
    @NotNull
    private Long targetDocumentId;
    private Long targetVersionId;
    private Long sourceArticleId;
    private Long targetArticleId;
    @NotBlank
    private String relationType;
    private String relationDesc;
    private LocalDate relationDate;
}
