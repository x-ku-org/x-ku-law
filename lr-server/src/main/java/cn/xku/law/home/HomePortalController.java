package cn.xku.law.home;

import cn.xku.law.common.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页检索门户 API：聚合覆盖统计、今日重点、热点检索与最新更新。
 * 游客可访问（公开法规数据），用于落地首页 PortalView。
 */
@Tag(name = "首页门户")
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomePortalController {

    private final HomeOverviewService homeOverviewService;

    @Operation(summary = "首页门户聚合数据（游客可访问）")
    @GetMapping("/overview")
    public CommonResult<HomeOverviewVO> overview() {
        return CommonResult.success(homeOverviewService.overview());
    }
}
