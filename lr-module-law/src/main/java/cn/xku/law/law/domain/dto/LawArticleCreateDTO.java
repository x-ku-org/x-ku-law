package cn.xku.law.law.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LawArticleCreateDTO {

    @NotNull
    private Long documentId;
    @NotNull
    private Long versionId;
    private Long parentArticleId;
    private String articleNo;
    private String articleTitle;
    private String chapterNo;
    private String chapterTitle;
    private String sectionNo;
    private String sectionTitle;
    private Integer articleOrder;
    private Integer articleLevel;
    @NotBlank
    private String contentText;
    private Boolean obligationFlag;
    private Boolean penaltyFlag;
    private String status;
}
