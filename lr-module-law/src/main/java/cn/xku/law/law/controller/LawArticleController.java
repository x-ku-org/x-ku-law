package cn.xku.law.law.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.dto.LawArticleCreateDTO;
import cn.xku.law.law.domain.dto.LawArticleQueryDTO;
import cn.xku.law.law.domain.vo.LawArticleVO;
import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.law.service.LawArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "法规条款")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/articles")
@RequiredArgsConstructor
public class LawArticleController {

    private final LawArticleService lawArticleService;

    @Operation(summary = "分页查询法规条款")
    @GetMapping
    public CommonResult<PageResult<LawArticleVO>> page(@Valid LawArticleQueryDTO query) {
        return CommonResult.success(lawArticleService.pageArticles(query));
    }

    @Operation(summary = "查询法规条款详情")
    @GetMapping("/{id}")
    public CommonResult<LawArticleVO> get(@PathVariable Long id) {
        return CommonResult.success(lawArticleService.getArticleById(id));
    }

    @Operation(summary = "新建法规条款")
    @PreAuthorize("hasAuthority('law:article:create')")
    @OperLog(module = "法规条款", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody LawArticleCreateDTO dto) {
        return CommonResult.success(lawArticleService.createArticle(dto));
    }

    @Operation(summary = "更新法规条款")
    @PreAuthorize("hasAuthority('law:article:update')")
    @OperLog(module = "法规条款", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @Valid @RequestBody LawArticleCreateDTO dto) {
        lawArticleService.updateArticle(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除法规条款")
    @PreAuthorize("hasAuthority('law:article:delete')")
    @OperLog(module = "法规条款", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        lawArticleService.removeArticle(id);
        return CommonResult.success();
    }
}
