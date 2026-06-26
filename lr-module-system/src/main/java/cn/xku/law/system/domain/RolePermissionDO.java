package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 角色权限关联表，对应 lr_role_permission */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_role_permission")
public class RolePermissionDO extends BaseDO {

    private Long roleId;
    private Long permissionId;
}
