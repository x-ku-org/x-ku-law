package cn.xku.law.law.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.dto.LawRelationCreateDTO;
import cn.xku.law.law.domain.dto.LawRelationQueryDTO;
import cn.xku.law.law.domain.vo.LawRelationVO;
import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.law.service.LawRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "法规关系")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/relations")
@RequiredArgsConstructor
public class LawRelationController {

    private final LawRelationService lawRelationService;

    @Operation(summary = "分页查询法规关系")
    @GetMapping
    public CommonResult<PageResult<LawRelationVO>> page(@Valid LawRelationQueryDTO query) {
        return CommonResult.success(lawRelationService.pageRelations(query));
    }

    @Operation(summary = "查询法规关系详情")
    @GetMapping("/{id}")
    public CommonResult<LawRelationVO> get(@PathVariable Long id) {
        return CommonResult.success(lawRelationService.getRelationById(id));
    }

    @Operation(summary = "新建法规关系")
    @PreAuthorize("hasAuthority('law:relation:create')")
    @OperLog(module = "法规关系", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody LawRelationCreateDTO dto) {
        return CommonResult.success(lawRelationService.createRelation(dto));
    }

    @Operation(summary = "更新法规关系")
    @PreAuthorize("hasAuthority('law:relation:update')")
    @OperLog(module = "法规关系", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody LawRelationCreateDTO dto) {
        lawRelationService.updateRelation(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除法规关系")
    @PreAuthorize("hasAuthority('law:relation:delete')")
    @OperLog(module = "法规关系", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        lawRelationService.removeRelation(id);
        return CommonResult.success();
    }
}
