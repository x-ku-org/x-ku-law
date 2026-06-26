package cn.xku.law.workspace.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.workspace.domain.dto.FeedbackCreateDTO;
import cn.xku.law.workspace.domain.dto.FeedbackQueryDTO;
import cn.xku.law.workspace.domain.vo.FeedbackVO;
import cn.xku.law.workspace.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户反馈")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/workspace/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "分页查询反馈")
    @GetMapping
    public CommonResult<PageResult<FeedbackVO>> page(@Valid FeedbackQueryDTO query) {
        return CommonResult.success(feedbackService.pageFeedbacks(query));
    }

    @Operation(summary = "提交反馈")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody FeedbackCreateDTO dto) {
        return CommonResult.success(feedbackService.createFeedback(dto));
    }

    @Operation(summary = "删除反馈")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        feedbackService.removeFeedback(id);
        return CommonResult.success();
    }
}
