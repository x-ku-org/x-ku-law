package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 通知消息表，对应 lr_notification */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_notification")
public class NotificationDO extends BaseDO {

    /** system/alert/audit/task/ticket */
    private String notificationType;
    private String templateCode;
    private String title;
    private String content;
    private String refType;
    private Long refId;
    /** single/role/tenant/all */
    private String sendScope;
    /** pending/sent/failed */
    private String status;
    private LocalDateTime sendTime;
}
