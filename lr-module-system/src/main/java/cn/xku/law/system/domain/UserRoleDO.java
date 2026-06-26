package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户角色关联表，对应 lr_user_role */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_user_role")
public class UserRoleDO extends BaseDO {

    private Long userId;
    private Long roleId;
}
