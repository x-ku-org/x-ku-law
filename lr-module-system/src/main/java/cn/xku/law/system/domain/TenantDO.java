package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 租户表，对应 lr_tenant */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_tenant")
public class TenantDO extends BaseDO {

    private String tenantCode;
    private String tenantName;
    /** platform/enterprise/government/school/law_firm */
    private String tenantType;
    /** enabled/disabled/expired */
    private String status;
    private String contactName;
    private String contactMobile;
    private LocalDateTime expireTime;
    /** 0 = 不限制 */
    private Integer maxUsers;
    private String remark;
}
