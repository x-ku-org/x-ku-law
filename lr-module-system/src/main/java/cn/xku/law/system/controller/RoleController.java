package cn.xku.law.system.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.dto.RoleCreateDTO;
import cn.xku.law.system.domain.dto.RolePermissionAssignDTO;
import cn.xku.law.system.domain.dto.RoleQueryDTO;
import cn.xku.law.system.domain.vo.RoleVO;
import cn.xku.law.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping
    public CommonResult<PageResult<RoleVO>> page(@Valid RoleQueryDTO query) {
        return CommonResult.success(roleService.pageRoles(query));
    }

    @Operation(summary = "查询角色详情")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/{id}")
    public CommonResult<RoleVO> get(@PathVariable Long id) {
        return CommonResult.success(roleService.getRoleById(id));
    }

    @Operation(summary = "创建角色")
    @PreAuthorize("hasAuthority('system:role:create')")
    @OperLog(module = "角色管理", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        return CommonResult.success(roleService.createRole(dto));
    }

    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('system:role:update')")
    @OperLog(module = "角色管理", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody RoleCreateDTO dto) {
        roleService.updateRole(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @OperLog(module = "角色管理", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        roleService.removeRole(id);
        return CommonResult.success();
    }

    @Operation(summary = "查询角色已分配权限 ID")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/{id}/permissions")
    public CommonResult<List<Long>> permissionIds(@PathVariable Long id) {
        return CommonResult.success(roleService.getPermissionIds(id));
    }

    @Operation(summary = "为角色分配权限")
    @PreAuthorize("hasAuthority('system:role:update')")
    @OperLog(module = "角色管理", type = "assign-permissions")
    @PutMapping("/{id}/permissions")
    public CommonResult<?> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionAssignDTO dto) {
        roleService.assignPermissions(id, dto.getPermissionIds());
        return CommonResult.success();
    }
}
