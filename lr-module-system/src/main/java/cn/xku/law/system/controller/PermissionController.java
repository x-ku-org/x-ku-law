package cn.xku.law.system.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.dto.PermissionCreateDTO;
import cn.xku.law.system.domain.dto.PermissionQueryDTO;
import cn.xku.law.system.domain.vo.PermissionVO;
import cn.xku.law.system.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/system/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "分页查询权限")
    @PreAuthorize("hasAuthority('system:permission:list')")
    @GetMapping
    public CommonResult<PageResult<PermissionVO>> page(@Valid PermissionQueryDTO query) {
        return CommonResult.success(permissionService.pagePermissions(query));
    }

    @Operation(summary = "查询全部权限树")
    @PreAuthorize("hasAuthority('system:permission:list')")
    @GetMapping("/all")
    public CommonResult<List<PermissionVO>> listAll() {
        return CommonResult.success(permissionService.listAll());
    }

    @Operation(summary = "创建权限")
    @PreAuthorize("hasAuthority('system:permission:create')")
    @OperLog(module = "权限管理", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody PermissionCreateDTO dto) {
        return CommonResult.success(permissionService.createPermission(dto));
    }

    @Operation(summary = "更新权限")
    @PreAuthorize("hasAuthority('system:permission:update')")
    @OperLog(module = "权限管理", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody PermissionCreateDTO dto) {
        permissionService.updatePermission(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除权限")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    @OperLog(module = "权限管理", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        permissionService.removePermission(id);
        return CommonResult.success();
    }
}
