package cn.xku.law.system.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 为角色分配权限：permissionIds 为目标权限全集（覆盖式保存，空集合表示清空权限）。 */
@Data
public class RolePermissionAssignDTO {
    private List<Long> permissionIds = new ArrayList<>();
}
