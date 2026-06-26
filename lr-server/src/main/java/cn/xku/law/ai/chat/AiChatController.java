package cn.xku.law.ai.chat;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 可溯源问答流式入口。与 lr-module-ai 的 AiMessageController 同享 /ai/messages 前缀，
 * 但因依赖检索/嵌入/law mappers 等基建置于 lr-server。
 */
@Tag(name = "AI 问答")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/ai/messages")
@RequiredArgsConstructor
public class AiChatController {

    /** SSE 超时（毫秒）：覆盖多轮工具检索 + 流式生成。 */
    private static final long SSE_TIMEOUT_MS = 180_000L;

    private final LawChatAgentService agentService;

    @Operation(summary = "流式可溯源问答（Agent 自主检索）")
    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ask(@Valid @RequestBody AskRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        agentService.stream(request, userId, SecurityContextHolder.getContext(), emitter);
        return emitter;
    }

    @Operation(summary = "请求当前流式问答停止检索、立即作答")
    @PostMapping("/stop")
    public CommonResult<Boolean> stop(@Valid @RequestBody StopAskDTO request) {
        agentService.requestStop(request.getStreamId());
        return CommonResult.success(true);
    }
}
