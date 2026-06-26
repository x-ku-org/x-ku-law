package cn.xku.law.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String roleType;
    private String dataScope;
    private String status;
    private Integer sortOrder;
    private String remark;
    private Long tenantId;
    private LocalDateTime createTime;
}
