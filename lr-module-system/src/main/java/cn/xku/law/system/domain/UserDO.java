package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 用户表，对应 lr_user */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_user")
public class UserDO extends BaseDO {

    private String username;
    private String passwordHash;
    private String realName;
    private String nickname;
    private String mobile;
    private String email;
    private String avatarUrl;
    private String gender;
    /** normal/admin/operator/auditor/customer_service */
    private String userType;
    /** enabled/disabled/locked */
    private String status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Long defaultOrgId;
    private LocalDateTime passwordUpdateTime;
    private String remark;
}
