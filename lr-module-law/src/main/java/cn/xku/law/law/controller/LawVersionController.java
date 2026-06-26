package cn.xku.law.law.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.law.domain.dto.LawVersionQueryDTO;
import cn.xku.law.law.domain.vo.LawVersionVO;
import cn.xku.law.law.service.LawVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "法规版本")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/versions")
@RequiredArgsConstructor
public class LawVersionController {

    private final LawVersionService lawVersionService;

    @Operation(summary = "分页查询法规版本")
    @GetMapping
    public CommonResult<PageResult<LawVersionVO>> page(@Valid LawVersionQueryDTO query) {
        return CommonResult.success(lawVersionService.pageVersions(query));
    }

    @Operation(summary = "查询法规版本详情")
    @GetMapping("/{id}")
    public CommonResult<LawVersionVO> get(@PathVariable Long id) {
        return CommonResult.success(lawVersionService.getVersionById(id));
    }

    @Operation(summary = "新建法规版本")
    @PreAuthorize("hasAuthority('law:version:create')")
    @OperLog(module = "法规版本", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody LawVersionCreateDTO dto) {
        return CommonResult.success(lawVersionService.createVersion(dto));
    }

    @Operation(summary = "更新法规版本")
    @PreAuthorize("hasAuthority('law:version:update')")
    @OperLog(module = "法规版本", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody LawVersionCreateDTO dto) {
        lawVersionService.updateVersion(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "发布法规版本")
    @PreAuthorize("hasAuthority('law:version:publish')")
    @OperLog(module = "法规版本", type = "publish")
    @PutMapping("/{id}/publish")
    public CommonResult<?> publish(@PathVariable Long id) {
        lawVersionService.publishVersion(id);
        return CommonResult.success();
    }

    @Operation(summary = "删除法规版本")
    @PreAuthorize("hasAuthority('law:version:delete')")
    @OperLog(module = "法规版本", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        lawVersionService.removeVersion(id);
        return CommonResult.success();
    }
}
