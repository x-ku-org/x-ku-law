package cn.xku.law.system.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.dto.UserCreateDTO;
import cn.xku.law.system.domain.dto.UserQueryDTO;
import cn.xku.law.system.domain.dto.UserRoleAssignDTO;
import cn.xku.law.system.domain.dto.UserUpdateDTO;
import cn.xku.law.system.domain.vo.UserVO;
import cn.xku.law.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping
    public CommonResult<PageResult<UserVO>> page(@Valid UserQueryDTO query) {
        return CommonResult.success(userService.pageUsers(query));
    }

    @Operation(summary = "查询用户详情")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/{id}")
    public CommonResult<UserVO> get(@PathVariable Long id) {
        return CommonResult.success(userService.getUserById(id));
    }

    @Operation(summary = "创建用户")
    @PreAuthorize("hasAuthority('system:user:create')")
    @OperLog(module = "用户管理", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return CommonResult.success(userService.createUser(dto));
    }

    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('system:user:update')")
    @OperLog(module = "用户管理", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @OperLog(module = "用户管理", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        userService.removeUser(id);
        return CommonResult.success();
    }

    @Operation(summary = "查询用户已分配角色 ID")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/{id}/roles")
    public CommonResult<List<Long>> roleIds(@PathVariable Long id) {
        return CommonResult.success(userService.getRoleIds(id));
    }

    @Operation(summary = "为用户分配角色")
    @PreAuthorize("hasAuthority('system:user:update')")
    @OperLog(module = "用户管理", type = "assign-roles")
    @PutMapping("/{id}/roles")
    public CommonResult<?> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleAssignDTO dto) {
        userService.assignRoles(id, dto.getRoleIds());
        return CommonResult.success();
    }
}
