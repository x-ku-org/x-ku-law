package cn.xku.law.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleCreateDTO {
    @NotBlank private String roleCode;
    @NotBlank private String roleName;
    private String roleType;
    private String dataScope;
    private String status;
    private Integer sortOrder;
    private String remark;
}
