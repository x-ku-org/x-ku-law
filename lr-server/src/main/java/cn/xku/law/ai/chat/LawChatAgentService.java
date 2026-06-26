package cn.xku.law.ai.chat;

import cn.xku.law.ai.domain.AiCitationDO;
import cn.xku.law.ai.domain.AiMessageDO;
import cn.xku.law.ai.domain.AiSessionDO;
import cn.xku.law.ai.domain.dto.AiAnswerCommand;
import cn.xku.law.ai.provider.AiChatModelRegistry;
import cn.xku.law.ai.service.AiMessageService;
import cn.xku.law.ai.service.AiSessionService;
import cn.xku.law.common.client.EmbeddingClient;
import cn.xku.law.common.client.SearchClient;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawInterpretationDO;
import cn.xku.law.law.mapper.LawArticleMapper;
import cn.xku.law.law.mapper.LawArticleSegmentMapper;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.service.LawInterpretationService;
import cn.xku.law.search.service.LawSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 可溯源问答的 Agent 编排：模型自主多轮调用 {@link LawSearchTools} 检索法规，
 * 据检索到的依据流式作答；回答与引用落库。运行在独立线程池上，
 * 显式传递并恢复 {@link SecurityContext}，以保证租户注入与审计字段填充正确。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LawChatAgentService {

    private static final Pattern CITE_PATTERN = Pattern.compile("\\[(\\d{1,3})]");

    private final AiChatModelRegistry chatModelRegistry;
    private final AiSessionService sessionService;
    private final AiMessageService messageService;
    private final LawSearchService lawSearchService;
    private final SearchClient searchClient;
    private final EmbeddingClient embeddingClient;
    private final LawArticleSegmentMapper segmentMapper;
    private final LawArticleMapper articleMapper;
    private final LawDocumentMapper documentMapper;
    private final LawInterpretationService interpretationService;
    private final ObjectMapper objectMapper;

    @Value("${app.vector.index-name:law_segment}")
    private String vectorIndex;

    @Value("${app.ai.default-provider:openai}")
    private String defaultProvider;

    /** 历史原文回灌的字符预算；超过则把较旧轮次折叠进滚动摘要。 */
    @Value("${app.ai.chat.context-char-budget:6000}")
    private int contextCharBudget;

    /** 至少保留多少条最近消息为原文（不被摘要吞掉）。 */
    @Value("${app.ai.chat.keep-recent-messages:6}")
    private int keepRecentMessages;

    /** 每次折叠进摘要的最旧消息条数。 */
    @Value("${app.ai.chat.summarize-batch-size:4}")
    private int summarizeBatchSize;

    /** 并发流式会话上限。 */
    @Value("${app.ai.chat.max-concurrency:8}")
    private int maxConcurrency;

    private ExecutorService executor;

    /** 在途流式问答的工具上下文注册表：streamId -> ctx，供「立即作答」按 streamId 置位停止标志。 */
    private final Map<String, AgentToolContext> activeStreams = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.executor = Executors.newFixedThreadPool(Math.max(1, maxConcurrency));
        log.info("[AiChat] agent service started (maxConcurrency={})", Math.max(1, maxConcurrency));
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) executor.shutdown();
    }

    /** 异步执行问答流；securityContext 在请求线程捕获，于工作线程恢复。 */
    public void stream(AskRequestDTO req, Long userId, SecurityContext securityContext, SseEmitter emitter) {
        executor.submit(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                doStream(req, userId, emitter);
            } catch (Exception e) {
                log.error("[AiChat] stream failed: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error")
                            .data("{\"message\":\"服务异常，请稍后重试\"}"));
                } catch (Exception ignore) {
                }
                emitter.completeWithError(e);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
    }

    /** 「立即作答」：按 streamId 置位停止标志，促使在途问答停止继续检索、用已有依据收尾（幂等）。 */
    public void requestStop(String streamId) {
        if (!StringUtils.hasText(streamId)) return;
        AgentToolContext ctx = activeStreams.get(streamId);
        if (ctx != null) {
            ctx.requestStop();
            log.info("[AiChat] stop requested for stream {}", streamId);
        }
    }

    private void doStream(AskRequestDTO req, Long userId, SseEmitter emitter) {
        AgentToolContext ctx = new AgentToolContext(req.getDocumentId(), emitter, objectMapper);
        String streamId = UUID.randomUUID().toString();
        activeStreams.put(streamId, ctx);
        try {
            doStreamInner(req, userId, emitter, ctx, streamId);
        } finally {
            activeStreams.remove(streamId);
        }
    }

    private void doStreamInner(AskRequestDTO req, Long userId, SseEmitter emitter,
                               AgentToolContext ctx, String streamId) {
        String provider = StringUtils.hasText(req.getProvider()) ? req.getProvider() : defaultProvider;
        boolean providerAvailable = chatModelRegistry.hasProvider(provider);

        Long sessionId = sessionService.ensureSession(req.getSessionId(), userId, req.getQuestion(),
                "qa", providerAvailable ? provider : null);
        Long userMessageId = messageService.appendUserMessage(sessionId, userId, req.getQuestion());
        ctx.send("meta", Map.of("sessionId", sessionId, "userMessageId", userMessageId, "streamId", streamId));

        if (!providerAvailable) {
            String refusal = "当前未配置可用的 AI 模型，暂时无法回答。请联系管理员开启模型后重试。";
            ctx.send("delta", Map.of("text", refusal));
            Long mid = messageService.appendAssistantMessage(AiAnswerCommand.builder()
                    .sessionId(sessionId).userId(userId).answerText(refusal)
                    .riskLevel("normal").citations(List.of()).build());
            sendDone(ctx, mid, List.of(), null, "normal");
            emitter.complete();
            return;
        }

        long start = System.currentTimeMillis();
        StringBuilder full = new StringBuilder();
        boolean firstTurn = false;
        String newTitle = null;
        ChatModel model = null;
        try {
            model = chatModelRegistry.getChatModel(provider);
            LawSearchTools tools = new LawSearchTools(ctx, lawSearchService, embeddingClient, searchClient,
                    segmentMapper, articleMapper, documentMapper, vectorIndex);
            HistoryContext history = buildHistory(model, sessionId, userMessageId);
            firstTurn = history.messages().isEmpty() && !StringUtils.hasText(history.summary());

            for (String chunk : ChatClient.create(model).prompt()
                    .system(systemPrompt(req.getDocumentId(), history.summary()))
                    .messages(history.messages())
                    .user(req.getQuestion())
                    .tools(tools)
                    .stream()
                    .content()
                    .toIterable()) {
                if (chunk == null || chunk.isEmpty()) continue;
                full.append(chunk);
                ctx.send("delta", Map.of("text", chunk));
            }
        } catch (Exception e) {
            log.error("[AiChat] generation failed: {}", e.getMessage(), e);
            if (full.length() == 0) {
                String msg = "回答生成失败：" + e.getMessage();
                ctx.send("delta", Map.of("text", msg));
                full.append(msg);
            }
        }

        int latency = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
        String answer = full.toString();
        if (!StringUtils.hasText(answer)) {
            answer = "未生成有效回答，请重试或调整问题。";
            ctx.send("delta", Map.of("text", answer));
        }
        List<AiCitationDO> citations = buildCitations(ctx, answer);
        String riskLevel = computeRiskLevel(answer, citations);
        Long assistantId = messageService.appendAssistantMessage(AiAnswerCommand.builder()
                .sessionId(sessionId).userId(userId).answerText(answer)
                .modelCode(provider).latencyMs(latency)
                .retrievalParamsJson(retrievalJson(ctx))
                .riskLevel(riskLevel).citations(citations).build());

        if (firstTurn && model != null) {
            newTitle = generateTitle(model, req.getQuestion(), answer);
            if (StringUtils.hasText(newTitle)) {
                sessionService.updateTitle(sessionId, newTitle);
            }
        }

        sendDone(ctx, assistantId, citations, newTitle, riskLevel);
        emitter.complete();
    }

    /** 历史上下文：滚动摘要（较旧轮次的压缩）+ 最近若干条原文消息。 */
    private record HistoryContext(String summary, List<Message> messages) {}

    /**
     * 构建多轮上下文：摘要覆盖之后的原文消息为「尾部」，超字符预算时自最旧端
     * 成批折叠进滚动摘要并落库，直到尾部进预算或仅剩 keepRecentMessages 条原文。
     */
    private HistoryContext buildHistory(ChatModel model, Long sessionId, Long excludeFromId) {
        AiSessionDO session = sessionService.getById(sessionId);
        String summary = (session != null && StringUtils.hasText(session.getContextSummary()))
                ? session.getContextSummary() : "";
        Long upto = session != null ? session.getSummaryUptoMessageId() : null;

        List<AiMessageDO> tail = messageService.lambdaQuery()
                .eq(AiMessageDO::getSessionId, sessionId)
                .lt(AiMessageDO::getId, excludeFromId)
                .gt(upto != null, AiMessageDO::getId, upto)
                .orderByAsc(AiMessageDO::getId)
                .list();

        boolean summaryChanged = false;
        while (tail.size() > keepRecentMessages
                && estimateChars(summary) + estimateChars(tail) > contextCharBudget) {
            int batch = Math.min(summarizeBatchSize, tail.size() - keepRecentMessages);
            if (batch <= 0) break;
            List<AiMessageDO> oldest = new ArrayList<>(tail.subList(0, batch));
            try {
                summary = summarizeOld(model, summary, oldest);
                upto = oldest.get(oldest.size() - 1).getId();
                summaryChanged = true;
            } catch (Exception e) {
                log.warn("[AiChat] summarize failed, drop oldest batch: {}", e.getMessage());
                tail.subList(0, batch).clear();
                break;
            }
            tail.subList(0, batch).clear();
        }
        if (summaryChanged) {
            sessionService.updateContextSummary(sessionId, summary, upto);
        }

        List<Message> messages = new ArrayList<>();
        for (AiMessageDO m : tail) {
            if ("assistant".equals(m.getMessageRole())) {
                if (StringUtils.hasText(m.getAnswerText())) {
                    messages.add(new AssistantMessage(m.getAnswerText()));
                }
            } else if (StringUtils.hasText(m.getQuestionText())) {
                messages.add(new UserMessage(m.getQuestionText()));
            }
        }
        return new HistoryContext(summary, messages);
    }

    private static int estimateChars(String s) {
        return s == null ? 0 : s.length();
    }

    private static int estimateChars(List<AiMessageDO> messages) {
        int total = 0;
        for (AiMessageDO m : messages) {
            total += estimateChars(m.getQuestionText()) + estimateChars(m.getAnswerText());
        }
        return total;
    }

    /** 增量摘要：把【已有摘要】与【新增对话】合并为更新后的摘要。 */
    private String summarizeOld(ChatModel model, String prevSummary, List<AiMessageDO> batch) {
        StringBuilder convo = new StringBuilder();
        for (AiMessageDO m : batch) {
            if ("assistant".equals(m.getMessageRole())) {
                convo.append("助手：").append(trim(m.getAnswerText(), 800)).append("\n");
            } else {
                convo.append("用户：").append(trim(m.getQuestionText(), 800)).append("\n");
            }
        }
        String prompt = """
                你在为一段「法律咨询多轮对话」维护滚动摘要，供后续轮次理解上下文与指代。
                请把【已有摘要】与【新增对话】合并为一份更新后的摘要，覆盖并保留以下关键信息：
                - 用户的咨询主题与具体诉求；
                - 限定条件（地域/行业/主体/情形/时间）；
                - 已给出的关键结论与建议；
                - 涉及到的法规名称及其 documentId（便于后续继续检索）；
                - 尚未解决或用户正在追问的问题。
                要求：忠实概括、不臆造新信息；简体中文，尽量精简（不超过 250 字）；直接输出摘要正文，不要前言或解释。

                【已有摘要】
                """ + (StringUtils.hasText(prevSummary) ? prevSummary : "（无）") + """

                【新增对话】
                """ + convo;
        String out = model.call(prompt);
        return StringUtils.hasText(out) ? out.trim() : prevSummary;
    }

    /** 首轮会话标题概括（失败返回 null，不影响主流程）。 */
    private String generateTitle(ChatModel model, String question, String answer) {
        try {
            String prompt = """
                    请为下面这轮法律咨询拟一个简短的会话标题，用于侧栏列表。
                    要求：概括「核心法律主题」（名词短语，而非问句），不超过 16 个汉字；
                    只输出标题本身，不要引号、书名号、句末标点或任何解释。

                    问题：""" + trim(question, 300) + """

                    回答摘要：""" + trim(answer, 300);
            String out = model.call(prompt);
            if (!StringUtils.hasText(out)) {
                return null;
            }
            String title = out.trim().replaceAll("\\s+", " ")
                    .replaceAll("^[\"'《]+", "").replaceAll("[\"'》。.]+$", "");
            return title.length() > 30 ? title.substring(0, 30) : title;
        } catch (Exception e) {
            log.warn("[AiChat] title generation failed: {}", e.getMessage());
            return null;
        }
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        String t = s.strip();
        return t.length() > max ? t.substring(0, max) : t;
    }

    /** 低置信度阈值（百分制）：被引用条款最高置信度低于此值时降为 warning。 */
    private static final int LOW_CONFIDENCE = 50;

    /**
     * 真实风险分级（替代此前硬编码 normal）：
     *   warning — 无引用支撑 / 正文无有效 [n] 引用 / 被引用项命中失效或废止版本 / 被引用项置信度过低；
     *   normal  — 有现行有效、置信度达标的引用支撑。
     */
    static String computeRiskLevel(String answer, List<AiCitationDO> citations) {
        if (citations == null || citations.isEmpty()) {
            return "warning"; // 无任何检索依据支撑
        }
        List<AiCitationDO> verified = citations.stream()
                .filter(c -> Boolean.TRUE.equals(c.getVerifiedFlag()))
                .collect(Collectors.toList());
        if (verified.isEmpty()) {
            return "warning"; // 正文未对应到任何账册引用，依据不足
        }
        boolean anyStale = verified.stream().anyMatch(c ->
                "repealed".equals(c.getValidityStatus()) || "superseded".equals(c.getValidityStatus()));
        if (anyStale) {
            return "warning"; // 引用了失效/废止版本，不能作为现行依据
        }
        BigDecimal maxConf = verified.stream()
                .map(AiCitationDO::getConfidenceScore).filter(Objects::nonNull)
                .max(BigDecimal::compareTo).orElse(null);
        if (maxConf != null && maxConf.compareTo(BigDecimal.valueOf(LOW_CONFIDENCE)) < 0) {
            return "warning"; // 语义召回置信度偏低
        }
        return "normal";
    }

    /** 引用校验：扫描回答中的 [n]，命中项标 verified；若无任何 [n] 命中则保留全部召回项。 */
    private List<AiCitationDO> buildCitations(AgentToolContext ctx, String answer) {
        List<AgentToolContext.CitationRef> refs = ctx.getRefs();
        if (refs.isEmpty()) {
            return List.of();
        }
        Set<Integer> cited = extractCitedSeqs(answer);
        boolean anyCited = !cited.isEmpty();
        List<AiCitationDO> result = new ArrayList<>();
        for (AgentToolContext.CitationRef ref : refs) {
            boolean isCited = cited.contains(ref.seq);
            if (anyCited && !isCited) {
                continue;
            }
            AiCitationDO citation = new AiCitationDO();
            citation.setRefType(ref.articleId != null ? "law_article" : "law_document");
            citation.setRefId(ref.articleId != null ? ref.articleId : ref.documentId);
            citation.setDocumentId(ref.documentId);
            citation.setVersionId(ref.versionId);
            citation.setArticleId(ref.articleId);
            citation.setQuoteText(ref.excerpt);
            citation.setSourceTitle(ref.lawTitle);
            citation.setArticleLabel(ref.articleLabel);
            citation.setConfidenceScore(ref.confidence);
            citation.setVerifiedFlag(isCited);
            citation.setValidityStatus(ref.validityStatus);
            // citationOrder 必须等于回答正文里的 [n] 编号（ref.seq），前端据此把 [n] 对应到账册条目；
            citation.setCitationOrder(ref.seq);
            result.add(citation);
        }
        return result;
    }

    private Set<Integer> extractCitedSeqs(String answer) {
        Set<Integer> seqs = new java.util.HashSet<>();
        if (!StringUtils.hasText(answer)) {
            return seqs;
        }
        Matcher matcher = CITE_PATTERN.matcher(answer);
        while (matcher.find()) {
            try {
                seqs.add(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignore) {
                // 忽略非法编号
            }
        }
        return seqs;
    }

    private void sendDone(AgentToolContext ctx, Long messageId, List<AiCitationDO> citations,
                          String title, String riskLevel) {
        List<Map<String, Object>> cites = citations.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            // 前端 cite 标识用「[n]」形式，须与正文里的 [n] 一致（n = citationOrder = ref.seq）。
            m.put("id", "[" + c.getCitationOrder() + "]");
            m.put("source", c.getSourceTitle());
            m.put("article", c.getArticleLabel());
            m.put("excerpt", c.getQuoteText());
            m.put("confidence", c.getConfidenceScore());
            m.put("documentId", c.getDocumentId());
            m.put("validityStatus", c.getValidityStatus());
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messageId", messageId);
        payload.put("citations", cites);
        payload.put("riskLevel", riskLevel);
        if (StringUtils.hasText(title)) {
            payload.put("title", title);
        }
        ctx.send("done", payload);
    }

    private String retrievalJson(AgentToolContext ctx) {
        try {
            return objectMapper.writeValueAsString(ctx.getRefs().stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("seq", r.seq);
                m.put("documentId", r.documentId);
                m.put("articleId", r.articleId);
                return m;
            }).toList());
        } catch (Exception e) {
            return null;
        }
    }

    private String systemPrompt(Long scopeDocumentId, String contextSummary) {
        String base = """
                # 角色
                你是「X-KU 法规智询」的资深法律研究助理，面向中国法律、法规、规章及规范性文件，提供**可溯源**的检索与解读。

                # 范围与边界
                - 只回答与法律法规相关的问题。对与法律无关、或属于个案诉讼代理/出具正式法律意见的请求，礼貌说明你仅基于公开法规提供研究性信息。
                - 你的回答是研究参考，不构成正式法律意见；涉及重大或高风险决策时，提示用户咨询执业律师（仅在确有必要时简短提示，不必每次都说）。

                # 可用工具（可多次、按需组合调用）
                - search_laws：发现/定位「有哪些相关法规」。不知道具体法规名、或要按效力级别/地区筛选时用；返回法规级条目，不含条文正文。
                - semantic_search_articles：按语义直达「最相关的具体条款」。回答一个具体问题时，这通常是首选起点；返回可直接引用的条款摘录。
                - read_law_articles：读取某部法规的条文原文（需 documentId）。在逐字引用前核对措辞、或需要通读相关条款时用。

                # 检索与作答流程
                1. 先判断意图，再检索后作答；**不要凭记忆直接回答**。
                2. 路径选择：问题具体 → 先 semantic_search_articles；只给主题/不确定是哪部法规 → 先 search_laws 定位，再对目标法规做语义检索或读原文。
                3. 需逐字引用条文时，用 read_law_articles 核对，按原文表述，不要改写后当作原文。
                4. 可多轮检索、交叉核对不同法规；但**已取得足够依据即停止**，不要重复相同查询或做无意义检索。
                5. 工具返回「未检索到/不可用」时：调整关键词或换工具再试；多次仍无结果，则按「依据不足」处理，不得编造。
                6. 地区适用：用户**未明确指定地区**时，以**全国性法规（法律、行政法规、部门规章）为主要依据**，不要主动检索、也不要把地方性法规/地方政府规章作为主要结论依据；某地方规定确有提示价值时仅作补充、并标明其地域适用范围。仅当用户指明地区、或问题本身就涉及某特定地方时，才用 search_laws 的 regionCode / 地方性 effectLevel 做地方检索。

                # 引用规则
                - 工具返回的每条依据都带 [n] 编号。回答中**每个关键结论后紧跟**其所依据的 [n]（可多个，如 [1][3]）。
                - [n] 必须是工具实际返回过的编号，**严禁臆造编号或引用未检索到的内容**；只引用你确实用到的依据，不堆砌无关编号。
                - 工具返回中标注「已废止/已失效」或「历史版本，已被新版取代」的条款，**不得作为现行有效依据**；如确需提及（如说明历史沿革），必须显式说明其已失效，并优先寻找现行有效的替代依据。

                # 输出要求
                - 简体中文 + Markdown。**结论先行**：先给直接答案，再列依据/条款，必要时附实务提示。
                - 忠于检索到的法规内容，不编造法规名、条号或结论；注意区分「现行有效」与可能已修订/废止的内容，拿不准就说明。
                - **不要输出检索过程或思考**，直接给最终回答；不要写「以下是回答」之类的开场白。
                - **不要使用 emoji 或任何表情符号**；只用规范中文标点与 《》、[n]。
                - 依据确实不足时，如实说明「现有法规依据不足」并指出还缺什么。
                """;
        if (scopeDocumentId != null) {
            base += buildScopeContext(scopeDocumentId);
        }
        if (StringUtils.hasText(contextSummary)) {
            base += "\n# 此前对话摘要（仅供你理解上下文与指代，勿直接复述给用户）\n" + contextSummary + "\n";
        }
        return base;
    }

    /** 从法规阅读页带入的聚焦范围：标题、元数据与解读摘要，供模型理解上下文。 */
    private String buildScopeContext(Long documentId) {
        LawDocumentDO doc = documentMapper.selectById(documentId);
        if (doc == null) {
            return "\n# 本次范围\n聚焦 documentId=" + documentId
                    + " 对应的法规：优先在该法规范围内检索与作答；确需引用其他法规时可补充检索。\n";
        }
        StringBuilder sb = new StringBuilder("\n# 本次范围\n");
        sb.append("用户从法规阅读页发起提问，当前聚焦法规：\n");
        sb.append("- 标题：《").append(doc.getTitle()).append("》\n");
        sb.append("- documentId：").append(documentId).append("\n");
        if (StringUtils.hasText(doc.getDocumentNo())) {
            sb.append("- 文号：").append(doc.getDocumentNo()).append("\n");
        }
        if (StringUtils.hasText(doc.getIssuingOrg())) {
            sb.append("- 发布机关：").append(doc.getIssuingOrg()).append("\n");
        }
        String timeliness = StringUtils.hasText(doc.getTimelinessStatus())
                ? doc.getTimelinessStatus() : doc.getStatus();
        if (StringUtils.hasText(timeliness)) {
            sb.append("- 时效：").append(timeliness).append("\n");
        }
        if (StringUtils.hasText(doc.getSummary())) {
            sb.append("- 摘要：").append(truncateContext(doc.getSummary(), 500)).append("\n");
        }
        if (doc.getCurrentVersionId() != null) {
            LawInterpretationDO interpretation = interpretationService.getByVersionId(doc.getCurrentVersionId());
            if (interpretation != null && StringUtils.hasText(interpretation.getInterpretationText())) {
                sb.append("- 智简解读：").append(truncateContext(interpretation.getInterpretationText(), 800)).append("\n");
            }
        }
        sb.append("优先在该法规范围内用 semantic_search_articles（documentId=").append(documentId)
                .append("）或 read_law_articles 检索与作答；确需引用其他法规时可补充检索。\n");
        return sb.toString();
    }

    private static String truncateContext(String text, int max) {
        if (text == null) return "";
        String t = text.strip().replaceAll("\\s+", " ");
        return t.length() > max ? t.substring(0, max) + "…" : t;
    }
}
