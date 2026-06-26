package cn.xku.law.ai.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.ai.domain.AiSessionDO;
import cn.xku.law.ai.domain.dto.AiSessionQueryDTO;
import cn.xku.law.ai.domain.vo.AiSessionVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AiSessionService extends IService<AiSessionDO> {

    PageResult<AiSessionVO> pageSessions(AiSessionQueryDTO query);

    AiSessionVO getSessionById(Long id);

    void removeSession(Long id);

    /**
     * 确保会话存在：sessionId 为空则按首问新建会话，非空则校验归属。
     * 同时刷新 lastMessageTime，返回有效 sessionId。
     */
    Long ensureSession(Long sessionId, Long userId, String firstQuestion, String scenarioType, String modelCode);

    /** 更新会话标题（首轮 LLM 概括后覆盖占位标题）。 */
    void updateTitle(Long sessionId, String title);

    /** 更新滚动摘要与其覆盖到的消息 ID（上下文压缩用）。 */
    void updateContextSummary(Long sessionId, String summary, Long summaryUptoMessageId);
}
