package cn.xku.law.workspace.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackCreateDTO {

    @NotBlank
    private String feedbackType;
    private String refType;
    private Long refId;
    @NotBlank
    private String content;
}
