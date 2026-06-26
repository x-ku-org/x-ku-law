package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 权限资源表，对应 lr_permission */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_permission")
public class PermissionDO extends BaseDO {

    private Long parentId;
    private String permissionCode;
    private String permissionName;
    /** menu/button/api/data */
    private String permissionType;
    private String path;
    private String component;
    private String requestMethod;
    private Integer sortOrder;
    /** 是否可见（bit → Boolean） */
    private Boolean visible;
    /** enabled/disabled */
    private String status;
}
