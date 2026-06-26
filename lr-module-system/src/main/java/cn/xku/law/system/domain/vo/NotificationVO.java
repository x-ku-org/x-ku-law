package cn.xku.law.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String notificationType;
    private String title;
    private String content;
    private String refType;
    private Long refId;
    private String sendScope;
    private String status;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
}
