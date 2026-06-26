package cn.xku.law.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionCreateDTO {
    private Long parentId;
    @NotBlank private String permissionCode;
    @NotBlank private String permissionName;
    private String permissionType;
    private String path;
    private String component;
    private String requestMethod;
    private Integer sortOrder;
    private Boolean visible;
    private String status;
}
