package cn.xku.law.workspace.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.workspace.domain.dto.FavoriteCreateDTO;
import cn.xku.law.workspace.domain.dto.FavoriteQueryDTO;
import cn.xku.law.workspace.domain.vo.FavoriteVO;
import cn.xku.law.workspace.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收藏管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/workspace/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "分页查询收藏")
    @GetMapping
    public CommonResult<PageResult<FavoriteVO>> page(@Valid FavoriteQueryDTO query) {
        return CommonResult.success(favoriteService.pageFavorites(query));
    }

    @Operation(summary = "添加收藏")
    @PostMapping
    public CommonResult<Long> add(@Valid @RequestBody FavoriteCreateDTO dto) {
        return CommonResult.success(favoriteService.addFavorite(dto));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        favoriteService.removeFavorite(id);
        return CommonResult.success();
    }
}
