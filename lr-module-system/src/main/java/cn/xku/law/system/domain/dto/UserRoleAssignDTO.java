package cn.xku.law.system.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 为用户分配角色：roleIds 为目标角色全集（覆盖式保存，空集合表示清空角色）。 */
@Data
public class UserRoleAssignDTO {
    private List<Long> roleIds = new ArrayList<>();
}
