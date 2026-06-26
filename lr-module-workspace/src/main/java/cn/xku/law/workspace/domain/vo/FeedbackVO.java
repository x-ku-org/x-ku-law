package cn.xku.law.workspace.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackVO {
    private Long id;
    private Long userId;
    private String feedbackType;
    private String refType;
    private Long refId;
    private String content;
    private String status;
    private Long handlerUserId;
    private LocalDateTime handledTime;
    private String resultDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
