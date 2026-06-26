package cn.xku.law.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationInboxVO {
    /** lr_notification_receiver.id */
    private Long receiverId;
    private Long notificationId;
    private String notificationType;
    private String title;
    private String content;
    private String refType;
    private Long refId;
    /** unread / read */
    private String readStatus;
    private LocalDateTime readTime;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
}
