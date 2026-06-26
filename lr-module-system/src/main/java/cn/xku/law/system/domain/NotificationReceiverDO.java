package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 通知接收人表，对应 lr_notification_receiver */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_notification_receiver")
public class NotificationReceiverDO extends BaseDO {

    private Long notificationId;
    private Long userId;
    /** station/email/sms */
    private String channel;
    private String receiver;
    /** unread/read */
    private String readStatus;
    private LocalDateTime readTime;
    /** pending/sent/failed */
    private String sendStatus;
    private String failReason;
}
