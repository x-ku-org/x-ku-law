package cn.xku.law.subscription.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 预警发送记录，对应 lr_alert_delivery */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_alert_delivery")
public class AlertDeliveryDO extends BaseDO {

    private Long ruleId;
    private Long matchId;
    private Long userId;
    /** station/email/sms/webhook */
    private String channel;
    private String receiver;
    /** pending/sent/failed */
    private String sendStatus;
    private LocalDateTime sendTime;
    private String failReason;
    private Integer retryCount;
}
