package cn.xku.law.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiFeedbackVO {
    private Long id;
    private Long messageId;
    private Long userId;
    private String feedbackType;
    private Integer rating;
    private String feedbackContent;
    private String handledStatus;
    private Long handledUserId;
    private LocalDateTime handledTime;
    private LocalDateTime createTime;
}
