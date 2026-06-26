package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 登录日志，对应 lr_login_log */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_login_log")
public class LoginLogDO extends BaseDO {

    private Long userId;
    private String username;
    /** password/sms/oauth */
    private String loginType;
    /** success/fail */
    private String loginStatus;
    private String ip;
    private String location;
    private String userAgent;
    private LocalDateTime loginTime;
    private String failReason;
}
