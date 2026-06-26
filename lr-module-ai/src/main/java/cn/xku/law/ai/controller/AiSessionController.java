package cn.xku.law.ai.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.ai.domain.dto.AiSessionQueryDTO;
import cn.xku.law.ai.domain.vo.AiSessionVO;
import cn.xku.law.ai.service.AiSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 会话")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/ai/sessions")
@RequiredArgsConstructor
public class AiSessionController {

    private final AiSessionService aiSessionService;

    @Operation(summary = "分页查询 AI 会话")
    @GetMapping
    public CommonResult<PageResult<AiSessionVO>> page(@Valid AiSessionQueryDTO query) {
        return CommonResult.success(aiSessionService.pageSessions(query));
    }

    @Operation(summary = "查询 AI 会话详情")
    @GetMapping("/{id}")
    public CommonResult<AiSessionVO> get(@PathVariable Long id) {
        return CommonResult.success(aiSessionService.getSessionById(id));
    }

    @Operation(summary = "删除 AI 会话")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        aiSessionService.removeSession(id);
        return CommonResult.success();
    }
}
