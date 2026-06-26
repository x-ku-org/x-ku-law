package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 角色表，对应 lr_role */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_role")
public class RoleDO extends BaseDO {

    private String roleCode;
    private String roleName;
    /** system/custom/tenant */
    private String roleType;
    /** all/tenant/org/self/custom */
    private String dataScope;
    /** enabled/disabled */
    private String status;
    private Integer sortOrder;
    private String remark;
}
