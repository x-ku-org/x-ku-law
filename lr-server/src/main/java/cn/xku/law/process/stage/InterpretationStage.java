package cn.xku.law.process.stage;

import cn.xku.law.ai.provider.AiChatModelRegistry;
import cn.xku.law.law.service.LawInterpretationService;
import cn.xku.law.process.LawProcessingContext;
import cn.xku.law.process.LawProcessingStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 阶段 40：AI 法规解读（整篇文档级）。用 LLM 从正文生成一篇面向读者的解读，
 * 落 {@code lr_law_interpretation}（按版本幂等）。
 *
 * <p>AI 阶段（{@link #requiresAi()}=true）：不在结构化主管线内联执行，由
 * {@code LawAiTaskProcessor} 旁路消费——彼时上下文正文由处理器从 {@code lr_law_version.content_text} 回灌。
 *
 * <p>与元数据富集的 best-effort 不同：解读是 AI 旁路任务的主职，
 * <b>无可用 provider / LLM 调用失败时抛出异常</b>，让 AI 任务失败可见并按重试策略重跑；
 * 仅在「无正文」或「模型返回空」这类无意义重试场景下记录日志后跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterpretationStage implements LawProcessingStage {

    @Value("${app.process.interpretation.max-input-chars:8000}")
    private int maxInputChars;

    /** 仅作落库标识，记录生成所用默认 provider。 */
    @Value("${app.ai.default-provider:openai}")
    private String defaultProvider;

    private final AiChatModelRegistry chatModelRegistry;
    private final LawInterpretationService interpretationService;

    @Override
    public String name() {
        return "interpretation";
    }

    @Override
    public int order() {
        return 40;
    }

    /** AI 阶段：由 LawAiTaskProcessor 旁路消费，不在结构化主管线内联跑。 */
    @Override
    public boolean requiresAi() {
        return true;
    }

    @Override
    public void process(LawProcessingContext ctx) {
        String text = ctx.getExtractedText();
        if (!StringUtils.hasText(text)) {
            log.debug("[Interpretation] versionId={} has no text, skip (仅元数据接入)", ctx.getVersionId());
            return;
        }

        ChatModel chatModel = chatModelRegistry.getDefaultChatModel();

        String raw = chatModel.call(buildPrompt(text));
        if (!StringUtils.hasText(raw)) {
            log.warn("[Interpretation] versionId={} model returned empty, skip save", ctx.getVersionId());
            return;
        }

        interpretationService.saveForVersion(ctx.getDocumentId(), ctx.getVersionId(), defaultProvider, raw.trim());
        log.info("[Interpretation] versionId={} doc={} interpreted ({} chars)",
                ctx.getVersionId(), ctx.getDocumentId(), raw.trim().length());
    }

    private String buildPrompt(String text) {
        String body = text.length() > maxInputChars ? text.substring(0, maxInputChars) : text;
        return """
                你是资深法律研究助理。请基于下面给出的法规/规章/规范性文件正文，撰写一篇面向普通读者的中文解读，
                帮助读者快速理解这部文件。请用简体中文、Markdown 小标题分段，覆盖：

                1. 立法目的与适用范围：这部文件解决什么问题、适用于谁、适用于什么情形；
                2. 核心制度与主要义务：梳理关键条款确立的主要制度、权利义务、禁止性规定或许可/处罚机制；
                3. 重点条款解读：挑选 3~6 个最重要或最易误解的条款，逐条说明其含义与实务影响；
                4. 实务影响与注意事项：对监管对象、企业或个人在合规上的提示。

                要求：忠于正文，不臆造正文未体现的结论；不复述与原文无关的背景；若正文信息不足，就已有内容如实概括，不编造。
                直接输出解读正文，不要前言、不要“以下是解读”之类的套话。

                正文：
                """ + body;
    }
}
