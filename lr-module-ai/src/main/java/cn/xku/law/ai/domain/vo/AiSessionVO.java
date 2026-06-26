package cn.xku.law.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiSessionVO {
    private Long id;
    private Long userId;
    /** 会话标题（由 DO.sessionTitle 映射）。 */
    private String title;
    private String scenarioType;
    private String modelCode;
    private String status;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
