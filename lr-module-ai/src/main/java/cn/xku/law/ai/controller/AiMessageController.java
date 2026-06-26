package cn.xku.law.ai.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.ai.domain.dto.AiMessageQueryDTO;
import cn.xku.law.ai.domain.vo.AiMessageVO;
import cn.xku.law.ai.service.AiMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 消息")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/ai/messages")
@RequiredArgsConstructor
public class AiMessageController {

    private final AiMessageService aiMessageService;

    @Operation(summary = "分页查询 AI 消息")
    @GetMapping
    public CommonResult<PageResult<AiMessageVO>> page(@Valid AiMessageQueryDTO query) {
        return CommonResult.success(aiMessageService.pageMessages(query));
    }

    // 流式问答入口 POST /ai/messages/ask 由 lr-server 的 AiChatController 提供
    // （需 SearchClient/EmbeddingClient/law mappers，置于 lr-server 以复用既有基建）。
}
