package cn.xku.law.search.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.search.domain.dto.SavedSearchCreateDTO;
import cn.xku.law.search.domain.dto.SavedSearchQueryDTO;
import cn.xku.law.search.domain.vo.SavedSearchVO;
import cn.xku.law.search.service.SavedSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "保存检索")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/search/saved")
@RequiredArgsConstructor
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @Operation(summary = "分页查询保存检索")
    @GetMapping
    public CommonResult<PageResult<SavedSearchVO>> page(@Valid SavedSearchQueryDTO query) {
        return CommonResult.success(savedSearchService.pageSavedSearches(query));
    }

    @Operation(summary = "新建保存检索")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody SavedSearchCreateDTO dto) {
        return CommonResult.success(savedSearchService.createSavedSearch(dto));
    }

    @Operation(summary = "更新保存检索")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody SavedSearchCreateDTO dto) {
        savedSearchService.updateSavedSearch(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除保存检索")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        savedSearchService.removeSavedSearch(id);
        return CommonResult.success();
    }
}
