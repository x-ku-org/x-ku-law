package cn.xku.law.subscription.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.subscription.domain.dto.SubscriptionMatchQueryDTO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleCreateDTO;
import cn.xku.law.subscription.domain.dto.SubscriptionRuleQueryDTO;
import cn.xku.law.subscription.domain.vo.SubscriptionMatchVO;
import cn.xku.law.subscription.domain.vo.SubscriptionRuleVO;
import cn.xku.law.subscription.service.SubscriptionMatchService;
import cn.xku.law.subscription.service.SubscriptionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订阅管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionRuleService ruleService;
    private final SubscriptionMatchService matchService;

    // ===== 订阅规则 =====

    @Operation(summary = "分页查询订阅规则")
    @GetMapping("/rules")
    public CommonResult<PageResult<SubscriptionRuleVO>> pageRules(@Valid SubscriptionRuleQueryDTO query) {
        return CommonResult.success(ruleService.pageRules(query));
    }

    @Operation(summary = "新建订阅规则")
    @PostMapping("/rules")
    public CommonResult<Long> createRule(@Valid @RequestBody SubscriptionRuleCreateDTO dto) {
        return CommonResult.success(ruleService.createRule(dto));
    }

    @Operation(summary = "更新订阅规则")
    @PutMapping("/rules/{id}")
    public CommonResult<?> updateRule(@PathVariable Long id,
                                      @Valid @RequestBody SubscriptionRuleCreateDTO dto) {
        ruleService.updateRule(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除订阅规则")
    @DeleteMapping("/rules/{id}")
    public CommonResult<?> removeRule(@PathVariable Long id) {
        ruleService.removeRule(id);
        return CommonResult.success();
    }

    // ===== 订阅命中 =====

    @Operation(summary = "分页查询订阅命中")
    @GetMapping("/matches")
    public CommonResult<PageResult<SubscriptionMatchVO>> pageMatches(@Valid SubscriptionMatchQueryDTO query) {
        return CommonResult.success(matchService.pageMatches(query));
    }

    @Operation(summary = "标记命中为已读")
    @PutMapping("/matches/{id}/read")
    public CommonResult<?> markMatchRead(@PathVariable Long id) {
        matchService.markRead(id);
        return CommonResult.success();
    }

    @Operation(summary = "全部命中标记为已读")
    @PutMapping("/matches/read-all")
    public CommonResult<Long> markAllMatchesRead() {
        return CommonResult.success(matchService.markAllRead());
    }

    @Operation(summary = "未读命中数")
    @GetMapping("/matches/unread-count")
    public CommonResult<Long> matchUnreadCount() {
        return CommonResult.success(matchService.countUnread());
    }
}
