package cn.xku.law.ai.chat;

import cn.xku.law.common.client.EmbeddingClient;
import cn.xku.law.common.client.SearchClient;
import cn.xku.law.common.client.dto.VectorMatch;
import cn.xku.law.law.domain.LawArticleDO;
import cn.xku.law.law.domain.LawArticleSegmentDO;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.mapper.LawArticleMapper;
import cn.xku.law.law.mapper.LawArticleSegmentMapper;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.search.domain.dto.LawSearchQueryDTO;
import cn.xku.law.search.domain.vo.LawSearchPageVO;
import cn.xku.law.search.domain.vo.LawSearchResultVO;
import cn.xku.law.search.service.LawSearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Agent 的法规检索工具集（每次请求新建实例，绑定 {@link AgentToolContext}）。
 * 三个工具映射真实法律检索的三种模式：定位法规 / 语义直达条款 / 读取条文原文。
 * 工具返回串中的 [n] 与上下文引用账册一致，供模型在回答中以 [n] 标注引用。
 */
@Slf4j
@RequiredArgsConstructor
public class LawSearchTools {

    private static final int MAX_EXCERPT = 240;

    /** 用户点「立即作答」后，工具短路返回的指令：让模型停止检索、基于已有依据收尾。 */
    private static final String STOP_DIRECTIVE =
            "用户已请求立即作答。请不要再调用任何检索工具，直接基于你已经获取到的检索结果回答；"
          + "若已掌握的依据不足以充分回答，请如实说明缺少哪些依据，不要编造。";

    private final AgentToolContext ctx;
    private final LawSearchService lawSearchService;
    private final EmbeddingClient embeddingClient;
    private final SearchClient searchClient;
    private final LawArticleSegmentMapper segmentMapper;
    private final LawArticleMapper articleMapper;
    private final LawDocumentMapper documentMapper;
    private final String vectorIndex;

    @Tool(description = "发现/定位法规：按关键词找出『有哪些相关法规文件』。当你还不知道具体法规名称、或需要按效力级别/地区筛选时使用。返回法规级条目（标题、文号、发布机构、效力级别、状态、documentId），不含条文正文——要读正文请改用 read_law_articles。可多次调用以尝试不同关键词。")
    public String searchLaws(
            @ToolParam(description = "检索关键词，用简洁法律术语或主题词（如『数据出境』『个人信息保护』），不要整句话") String keyword,
            @ToolParam(required = false, description = "效力级别精确过滤，取值如：法律 / 行政法规 / 部门规章 / 地方性法规 / 地方政府规章") String effectLevel,
            @ToolParam(required = false, description = "适用地区行政区划代码，用于筛选地方性法规") String regionCode,
            @ToolParam(required = false, description = "返回条数，默认 5，最大 10") Integer limit) {
        if (ctx.shouldStop()) return STOP_DIRECTIVE;
        ctx.send("tool", Map.of("tool", "search_laws", "summary", "检索法规：" + safe(keyword)));
        try {
            LawSearchQueryDTO query = new LawSearchQueryDTO();
            query.setKeyword(keyword);
            query.setEffectLevel(effectLevel);
            query.setRegionCode(regionCode);
            query.setPageNo(1);
            query.setPageSize(clamp(limit, 5, 10));
            LawSearchPageVO page = lawSearchService.search(query);
            List<LawSearchResultVO> list = page.getList();
            if (list == null || list.isEmpty()) {
                return "未检索到与「" + safe(keyword) + "」相关的法规。";
            }
            StringBuilder sb = new StringBuilder("检索到以下法规：\n");
            for (LawSearchResultVO r : list) {
                LawDocumentDO doc = documentMapper.selectById(r.getDocumentId());
                String validity = validityOf(doc, r.getVersionId());
                int n = ctx.register(r.getDocumentId(), r.getVersionId(), null, r.getTitle(), null, null, null, validity);
                sb.append("[").append(n).append("] 《").append(r.getTitle()).append("》");
                if (StringUtils.hasText(r.getDocNumber())) sb.append(" ").append(r.getDocNumber());
                if (StringUtils.hasText(r.getEffectLevel())) sb.append(" · ").append(r.getEffectLevel());
                if (StringUtils.hasText(r.getPublishAuthority())) sb.append(" · ").append(r.getPublishAuthority());
                sb.append("（documentId=").append(r.getDocumentId()).append("）").append(validityNote(validity)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("[AiChat] searchLaws failed: {}", e.getMessage());
            return "法规检索服务暂不可用：" + e.getMessage();
        }
    }

    @Tool(description = "语义检索条款：按问题含义直达最相关的『具体条文片段』，是回答具体问题的首选起点。返回条款所属法规、条号与原文摘录，可直接引用。可传 documentId 限定到某部法规。可多次调用以细化查询。")
    public String semanticSearchArticles(
            @ToolParam(description = "自然语言描述你要找的条款内容，尽量贴近用户的具体诉求（如『跨境提供个人信息的安全评估义务』）") String query,
            @ToolParam(required = false, description = "限定只在该法规内做语义检索，传 documentId") Long documentId,
            @ToolParam(required = false, description = "返回条数，默认 6，最大 10") Integer topK) {
        if (ctx.shouldStop()) return STOP_DIRECTIVE;
        ctx.send("tool", Map.of("tool", "semantic_search_articles", "summary", "语义检索条款：" + safe(query)));
        Long scope = documentId != null ? documentId : ctx.getScopeDocumentId();
        try {
            float[] vector = embeddingClient.embed(query);
            int k = clamp(topK, 6, 10);
            // 限定法规时多取一些候选再按 documentId 过滤
            List<VectorMatch> matches = searchClient.vectorSearchScored(vectorIndex, vector, scope != null ? k * 4 : k);
            if (matches.isEmpty()) {
                return "语义检索未返回结果（向量库可能未启用或暂无数据）。";
            }
            Map<Long, LawArticleSegmentDO> segMap = new HashMap<>();
            for (LawArticleSegmentDO seg : segmentMapper.selectBatchIds(
                    matches.stream().map(m -> Long.valueOf(m.id())).toList())) {
                segMap.put(seg.getId(), seg);
            }
            Map<Long, LawDocumentDO> docCache = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            int shown = 0;
            for (VectorMatch match : matches) {
                LawArticleSegmentDO seg = segMap.get(Long.valueOf(match.id()));
                if (seg == null) continue;
                LawArticleDO article = articleMapper.selectById(seg.getArticleId());
                if (article == null) continue;
                Long docId = article.getDocumentId();
                if (scope != null && !scope.equals(docId)) continue;
                LawDocumentDO doc = docCache.computeIfAbsent(docId, documentMapper::selectById);
                String title = doc != null ? doc.getTitle() : ("法规#" + docId);
                String label = StringUtils.hasText(article.getArticleNo()) ? article.getArticleNo() : "";
                String excerpt = truncate(seg.getSegmentText());
                String validity = validityOf(doc, seg.getVersionId());
                int n = ctx.register(docId, seg.getVersionId(), article.getId(), title, label, excerpt,
                        scoreToPercent(match.score()), validity);
                sb.append("[").append(n).append("] 《").append(title).append("》")
                        .append(label).append("：").append(excerpt).append(validityNote(validity)).append("\n");
                if (++shown >= k) break;
            }
            if (shown == 0) {
                return scope != null ? "在指定法规内未检索到相关条款。" : "未检索到相关条款。";
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("[AiChat] semanticSearchArticles failed: {}", e.getMessage());
            return "语义检索暂不可用（嵌入或向量服务未启用）：" + e.getMessage();
        }
    }

    @Tool(description = "读取条文原文：按 documentId 取出某部法规的具体条文，用于在逐字引用前核对措辞、或通读相关条款。documentId 通常来自 search_laws / semantic_search_articles 的返回。可选 keyword 只取包含该词的条款。可多次调用。")
    public String readLawArticles(
            @ToolParam(description = "法规文档 ID（来自其他工具返回的 documentId）") Long documentId,
            @ToolParam(required = false, description = "只返回内容包含该关键词的条款；不传则按条文顺序返回") String keyword,
            @ToolParam(required = false, description = "最多返回条款数，默认 8，最大 20") Integer limit) {
        if (ctx.shouldStop()) return STOP_DIRECTIVE;
        try {
            LawDocumentDO doc = documentMapper.selectById(documentId);
            if (doc == null) {
                ctx.send("tool", Map.of("tool", "read_law_articles", "summary", "读取条文（未找到 documentId=" + documentId + "）"));
                return "未找到 documentId=" + documentId + " 的法规。";
            }
            String hint = "读取条文《" + doc.getTitle() + "》"
                    + (StringUtils.hasText(keyword) ? "（含「" + keyword + "」）" : "");
            ctx.send("tool", Map.of("tool", "read_law_articles", "summary", hint));
            int max = clamp(limit, 8, 20);
            LambdaQueryWrapper<LawArticleDO> wrapper = new LambdaQueryWrapper<LawArticleDO>()
                    .eq(LawArticleDO::getDocumentId, documentId);
            if (doc.getCurrentVersionId() != null) {
                wrapper.eq(LawArticleDO::getVersionId, doc.getCurrentVersionId());
            }
            if (StringUtils.hasText(keyword)) {
                wrapper.like(LawArticleDO::getContentText, keyword);
            }
            wrapper.orderByAsc(LawArticleDO::getArticleOrder).last("LIMIT " + max);
            List<LawArticleDO> articles = articleMapper.selectList(wrapper);
            if (articles.isEmpty()) {
                return "该法规下未找到匹配条款。";
            }
            String validity = validityOf(doc, doc.getCurrentVersionId());
            StringBuilder sb = new StringBuilder("《").append(doc.getTitle()).append("》相关条文")
                    .append(validityNote(validity)).append("：\n");
            for (LawArticleDO a : articles) {
                String label = StringUtils.hasText(a.getArticleNo()) ? a.getArticleNo() : "";
                String text = truncate(a.getContentText());
                int n = ctx.register(documentId, a.getVersionId(), a.getId(), doc.getTitle(), label, text, null,
                        validityOf(doc, a.getVersionId()));
                sb.append("[").append(n).append("] ").append(label).append(" ").append(text).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("[AiChat] readLawArticles failed: {}", e.getMessage());
            return "读取条文失败：" + e.getMessage();
        }
    }

    /**
     * 判定被引用条款/法规的时效：
     *   repealed — 文档状态为已废止/已失效（repealed/expired）；
     *   superseded — 文档为历史版本（status=amended），或引用的不是文档当前有效版本；
     *   current — 现行有效。
     * 用于在工具返回中给非现行项加标注，避免模型把废止条款当现行依据。
     */
    static String validityOf(LawDocumentDO doc, Long citedVersionId) {
        if (doc == null) return "current";
        String status = doc.getStatus();
        if ("repealed".equals(status) || "expired".equals(status)) {
            return "repealed";
        }
        if ("amended".equals(status)) {
            return "superseded";
        }
        Long current = doc.getCurrentVersionId();
        if (citedVersionId != null && current != null && !citedVersionId.equals(current)) {
            return "superseded";
        }
        return "current";
    }

    /** 非现行时效的提示后缀，引导模型不当现行依据。 */
    private static String validityNote(String validity) {
        if ("repealed".equals(validity)) {
            return "（⚠ 已废止/已失效，非现行有效依据，仅供参考）";
        }
        if ("superseded".equals(validity)) {
            return "（⚠ 历史版本，已被新版取代，非现行有效依据）";
        }
        return "";
    }

    private static int clamp(Integer value, int def, int max) {
        if (value == null || value <= 0) return def;
        return Math.min(value, max);
    }

    /** 把向量相似度分值转为百分制置信度（前端按 % 展示）。
     *  cosine 相似度下 ES _score ∈ [0,1]，乘 100 落到 [0,100] 并夹紧。 */
    private static BigDecimal scoreToPercent(double score) {
        double pct = Math.max(0d, Math.min(100d, score * 100d));
        return BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP);
    }

    private static String truncate(String text) {
        if (text == null) return "";
        String t = text.strip().replaceAll("\\s+", " ");
        return t.length() > MAX_EXCERPT ? t.substring(0, MAX_EXCERPT) + "…" : t;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
