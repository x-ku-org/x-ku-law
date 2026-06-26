package cn.xku.law.system.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageParam {
    private String roleCode;
    private String roleName;
    private String status;
}
