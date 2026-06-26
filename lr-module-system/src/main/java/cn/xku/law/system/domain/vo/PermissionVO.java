package cn.xku.law.system.domain.vo;

import lombok.Data;

@Data
public class PermissionVO {
    private Long id;
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private String path;
    private String requestMethod;
    private Integer sortOrder;
    private Boolean visible;
    private String status;
}
