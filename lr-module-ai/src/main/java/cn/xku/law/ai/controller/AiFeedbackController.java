package cn.xku.law.ai.controller;

import cn.xku.law.ai.domain.dto.AiFeedbackCreateDTO;
import cn.xku.law.ai.domain.dto.AiFeedbackQueryDTO;
import cn.xku.law.ai.domain.vo.AiFeedbackVO;
import cn.xku.law.ai.service.AiFeedbackService;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 反馈")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/ai/feedback")
@RequiredArgsConstructor
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    @Operation(summary = "提交 AI 回答反馈（纠错等）")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody AiFeedbackCreateDTO dto) {
        return CommonResult.success(aiFeedbackService.createFeedback(dto));
    }

    @Operation(summary = "切换某条回答的点赞状态", description = "已赞则取消，未赞则点赞；返回切换后的状态")
    @PostMapping("/like/{messageId}")
    public CommonResult<Boolean> toggleLike(@PathVariable Long messageId) {
        return CommonResult.success(aiFeedbackService.toggleLike(messageId));
    }

    @Operation(summary = "分页查询 AI 反馈（管理员）")
    @PreAuthorize("hasAuthority('ai:feedback:manage')")
    @GetMapping
    public CommonResult<PageResult<AiFeedbackVO>> page(@Valid AiFeedbackQueryDTO query) {
        return CommonResult.success(aiFeedbackService.pageFeedbacks(query));
    }

    @Operation(summary = "处理 AI 反馈（管理员）", description = "handledStatus：processing/resolved/closed")
    @PreAuthorize("hasAuthority('ai:feedback:manage')")
    @PutMapping("/{id}/handle")
    public CommonResult<?> handle(@PathVariable Long id, @RequestParam String handledStatus) {
        aiFeedbackService.handleFeedback(id, handledStatus);
        return CommonResult.success();
    }
}
