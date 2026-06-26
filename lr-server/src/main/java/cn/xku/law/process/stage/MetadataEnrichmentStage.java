package cn.xku.law.process.stage;

import cn.xku.law.ai.provider.AiChatModelRegistry;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.service.LawDocumentService;
import cn.xku.law.law.service.LawTagAttacher;
import cn.xku.law.process.DataGovernanceRecorder;
import cn.xku.law.process.LawProcessingContext;
import cn.xku.law.process.LawProcessingStage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 阶段 25：AI 元数据富集。在分段(20)之后、发布(30)之前，用 LLM 从正文抽取
 * 文档级 {@code summary}（摘要）与一组关键词 {@code tags}，分别写回 {@code lr_law_document.summary}
 * 与 {@code lr_law_document_tag}。置于发布前，保证摘要/标签在建索引时即已就位、版本一致。
 *
 * <p>最佳努力（best-effort）：本阶段不抛异常打断管线——无可用 AI provider（registry 抛异常）、
 * 正文为空、LLM 调用失败或返回不可解析时，均记录日志并跳过，summary 留原值、不写标签。
 * 摘要仅在文档当前摘要为空时写入，避免覆盖人工录入；标签 attach 幂等可重复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataEnrichmentStage implements LawProcessingStage {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${app.process.metadata.max-input-chars:6000}")
    private int maxInputChars;

    private final AiChatModelRegistry chatModelRegistry;
    private final LawDocumentService lawDocumentService;
    private final LawTagAttacher lawTagAttacher;
    private final DataGovernanceRecorder governanceRecorder;

    @Override
    public String name() {
        return "metadata-enrichment";
    }

    @Override
    public int order() {
        return 25;
    }

    /** AI 阶段：不在结构化主管线内联跑，由 LawAiTaskProcessor 旁路消费。 */
    @Override
    public boolean requiresAi() {
        return true;
    }

    @Override
    public void process(LawProcessingContext ctx) {
        String text = ctx.getExtractedText();
        if (!StringUtils.hasText(text)) {
            log.debug("[Metadata] versionId={} has no extracted text, skip", ctx.getVersionId());
            return;
        }

        ChatModel chatModel;
        try {
            chatModel = chatModelRegistry.getDefaultChatModel();
        } catch (RuntimeException e) {
            log.info("[Metadata] versionId={} no AI provider available, skip enrichment ({})",
                    ctx.getVersionId(), e.getMessage());
            return;
        }

        try {
            String raw = chatModel.call(buildPrompt(text));
            JsonNode node = parseJson(raw);
            if (node == null) {
                log.warn("[Metadata] versionId={} AI response not parseable as JSON, skip", ctx.getVersionId());
                return;
            }
            String summary = trimToNull(node.path("summary").asText(null));
            List<String> tags = readTags(node);

            applySummary(ctx.getDocumentId(), summary);
            applyTags(ctx.getDocumentId(), tags);

            if (summary == null || tags.isEmpty()) {
                governanceRecorder.recordQualityIssue("law_document", ctx.getDocumentId(), "missing_field",
                        "low", "AI 富集后" + (summary == null ? "缺摘要" : "")
                                + (tags.isEmpty() ? "缺标签" : "") + "，建议人工补全");
            }

            log.info("[Metadata] versionId={} doc={} enriched (summary={}chars, tags={})",
                    ctx.getVersionId(), ctx.getDocumentId(),
                    summary == null ? 0 : summary.length(), tags.size());
        } catch (Exception e) {
            log.warn("[Metadata] versionId={} enrichment failed, skip: {}", ctx.getVersionId(), e.getMessage());
        }
    }

    /** 摘要仅在文档当前摘要为空时写入，避免覆盖人工录入。 */
    private void applySummary(Long documentId, String summary) {
        if (documentId == null || !StringUtils.hasText(summary)) {
            return;
        }
        LawDocumentDO doc = lawDocumentService.getById(documentId);
        if (doc == null || StringUtils.hasText(doc.getSummary())) {
            return;
        }
        LawDocumentDO update = new LawDocumentDO();
        update.setId(documentId);
        update.setSummary(summary);
        lawDocumentService.updateById(update);
    }

    private void applyTags(Long documentId, List<String> tags) {
        for (String tag : tags) {
            lawTagAttacher.attach(documentId, tag);
        }
    }

    private List<String> readTags(JsonNode node) {
        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = node.path("tags");
        if (tagsNode.isArray()) {
            for (JsonNode t : tagsNode) {
                String tag = trimToNull(t.asText(null));
                if (tag != null && !tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    private String buildPrompt(String text) {
        String body = text.length() > maxInputChars ? text.substring(0, maxInputChars) : text;
        return """
                你是法规知识库的元数据富集助手。系统正在把法规、规章、规范性文件或标准正文接入知识库；
                你的任务是在发布前为该文档补充可展示的摘要和可检索的自由标签。

                严格只输出一个 JSON 对象，不要解释、不要 Markdown、不要代码块围栏，格式如下：
                {"summary": "不超过200字的中文摘要", "tags": ["关键词1", "关键词2"]}

                要求：
                1. summary 面向法规列表/详情页展示，用简体中文概括该文件的调整对象、核心制度/义务、
                   适用范围或监管目标；不要编造正文没有体现的结论；
                2. tags 是自由关键词，用于检索、推荐和聚合；提取 3 到 8 个简体中文主题词，优先选择
                   法律领域、监管事项、适用对象、业务场景或风险类型，如「个人信息保护」「行政许可」
                   「食品安全」「安全生产」「网络安全」「生态环境」；
                3. tags 不要使用文件标题、发文字号、发布日期、生效日期、发布机关、地域名称，也不要输出
                   “法律”“法规”“标准”“规范性文件”等过宽泛词；
                4. tags 去重，尽量使用 2 到 8 个汉字的名词或短语，不要句子、编号或标点；
                5. 若正文信息不足，summary 设为空字符串、tags 设为空数组。

                正文：
                """ + body;
    }

    /** 容错解析：剥离可能的 ```json 围栏，截取首个 {...} 再解析。 */
    private static JsonNode parseJson(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String s = raw.trim();
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        String json = s.substring(start, end + 1);
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
