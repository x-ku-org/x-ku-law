package cn.xku.law.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiFeedbackCreateDTO {

    @NotNull
    private Long messageId;
    /** like/dislike/error/hallucination/missing_citation/escalate */
    @NotBlank
    private String feedbackType;
    /** 评分 1-5（可空） */
    private Integer rating;
    private String feedbackContent;
}
