package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户偏好表，对应 lr_user_preference（键值对，按 preference_group 分组） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_user_preference")
public class UserPreferenceDO extends BaseDO {

    private Long userId;
    private String preferenceKey;
    private String preferenceValue;
    private String preferenceGroup;
}
