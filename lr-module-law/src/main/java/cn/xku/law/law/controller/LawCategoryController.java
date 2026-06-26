package cn.xku.law.law.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.dto.LawCategoryCreateDTO;
import cn.xku.law.law.domain.dto.LawCategoryQueryDTO;
import cn.xku.law.law.domain.vo.LawCategoryVO;
import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.law.service.LawCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "法规分类")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/categories")
@RequiredArgsConstructor
public class LawCategoryController {

    private final LawCategoryService lawCategoryService;

    @Operation(summary = "分页查询法规分类")
    @GetMapping
    public CommonResult<PageResult<LawCategoryVO>> page(@Valid LawCategoryQueryDTO query) {
        return CommonResult.success(lawCategoryService.pageCategories(query));
    }

    @Operation(summary = "查询全部启用分类（前端树形下拉用）")
    @GetMapping("/all")
    public CommonResult<List<LawCategoryVO>> listAll() {
        return CommonResult.success(lawCategoryService.listAll());
    }

    @Operation(summary = "查询法规分类详情")
    @GetMapping("/{id}")
    public CommonResult<LawCategoryVO> get(@PathVariable Long id) {
        return CommonResult.success(lawCategoryService.getCategoryById(id));
    }

    @Operation(summary = "新建法规分类")
    @PreAuthorize("hasAuthority('law:category:create')")
    @OperLog(module = "法规分类", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody LawCategoryCreateDTO dto) {
        return CommonResult.success(lawCategoryService.createCategory(dto));
    }

    @Operation(summary = "更新法规分类")
    @PreAuthorize("hasAuthority('law:category:update')")
    @OperLog(module = "法规分类", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody LawCategoryCreateDTO dto) {
        lawCategoryService.updateCategory(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除法规分类")
    @PreAuthorize("hasAuthority('law:category:delete')")
    @OperLog(module = "法规分类", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        lawCategoryService.removeCategory(id);
        return CommonResult.success();
    }
}
