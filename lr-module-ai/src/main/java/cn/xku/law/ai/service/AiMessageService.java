package cn.xku.law.ai.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.ai.domain.AiMessageDO;
import cn.xku.law.ai.domain.dto.AiAnswerCommand;
import cn.xku.law.ai.domain.dto.AiMessageQueryDTO;
import cn.xku.law.ai.domain.vo.AiMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AiMessageService extends IService<AiMessageDO> {

    PageResult<AiMessageVO> pageMessages(AiMessageQueryDTO query);

    /** 落用户提问，返回 messageId。 */
    Long appendUserMessage(Long sessionId, Long userId, String question);

    /** 落助手回答 + 引用依据（事务内），返回 messageId。 */
    Long appendAssistantMessage(AiAnswerCommand command);
}
