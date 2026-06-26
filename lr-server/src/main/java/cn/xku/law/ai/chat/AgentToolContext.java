package cn.xku.law.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单次问答请求的工具上下文：在请求线程构造、随工具实例传递，
 * 承载检索范围、SSE 通道与跨多次工具调用的引用账册（全局 [n] 编号）。
 * 不依赖 ThreadLocal，故在流式/工具回调跨线程时仍然可用。
 */
@Slf4j
public class AgentToolContext {

    private final Long scopeDocumentId;
    private final SseEmitter emitter;
    private final ObjectMapper objectMapper;

    /** 引用账册（按发现顺序），seq 从 1 递增。 */
    private final List<CitationRef> refs = new ArrayList<>();
    /** 去重索引：documentId:articleId -> ref。 */
    private final Map<String, CitationRef> byKey = new LinkedHashMap<>();

    /**
     * 用户「立即作答」标志：置位后，工具被调用时直接短路返回停止指令，
     * 不再真正检索，促使模型基于已有依据收尾。volatile 保证跨线程可见
     * （stop 请求在 web 线程置位，工具在 worker 线程读取）。
     */
    private volatile boolean stopRequested = false;

    public AgentToolContext(Long scopeDocumentId, SseEmitter emitter, ObjectMapper objectMapper) {
        this.scopeDocumentId = scopeDocumentId;
        this.emitter = emitter;
        this.objectMapper = objectMapper;
    }

    public Long getScopeDocumentId() {
        return scopeDocumentId;
    }

    /** 请求停止继续检索、立即作答（幂等）。 */
    public void requestStop() {
        this.stopRequested = true;
    }

    /** 是否已请求立即作答。 */
    public boolean shouldStop() {
        return stopRequested;
    }

    public List<CitationRef> getRefs() {
        return refs;
    }

    /**
     * 注册一条引用并返回其全局序号 [n]；同一 (documentId, articleId) 复用既有序号。
     */
    public synchronized int register(Long documentId, Long versionId, Long articleId,
                                     String lawTitle, String articleLabel, String excerpt,
                                     BigDecimal confidence, String validityStatus) {
        String key = documentId + ":" + (articleId == null ? "" : articleId);
        CitationRef existing = byKey.get(key);
        if (existing != null) {
            return existing.seq;
        }
        CitationRef ref = new CitationRef();
        ref.seq = refs.size() + 1;
        ref.documentId = documentId;
        ref.versionId = versionId;
        ref.articleId = articleId;
        ref.lawTitle = lawTitle;
        ref.articleLabel = articleLabel;
        ref.excerpt = excerpt;
        ref.confidence = confidence;
        ref.validityStatus = validityStatus != null ? validityStatus : "current";
        refs.add(ref);
        byKey.put(key, ref);
        return ref.seq;
    }

    /** 发送一个 SSE 事件（data 序列化为 JSON 字符串）；失败仅记录，不打断主流程。 */
    public void send(String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(objectMapper.writeValueAsString(data)));
        } catch (Exception e) {
            log.debug("[AiChat] send SSE event '{}' failed: {}", event, e.getMessage());
        }
    }

    /** 引用条目（工具检索到的法规/条款）。 */
    public static class CitationRef {
        public int seq;
        public Long documentId;
        public Long versionId;
        public Long articleId;
        public String lawTitle;
        public String articleLabel;
        public String excerpt;
        public BigDecimal confidence;
        /** 时效：current（现行有效）/ superseded（历史版本）/ repealed（已废止/失效）。 */
        public String validityStatus = "current";
    }
}
