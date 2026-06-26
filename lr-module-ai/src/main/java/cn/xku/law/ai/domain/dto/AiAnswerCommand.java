package cn.xku.law.ai.domain.dto;

import cn.xku.law.ai.domain.AiCitationDO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 助手回答落库命令：由 Agent 编排层（lr-server）组装，
 * 一次性把回答正文 + 引用依据 + 计量信息交给 AI 模块持久化。
 */
@Data
@Builder
public class AiAnswerCommand {
    private Long sessionId;
    private Long userId;
    private String answerText;
    private String modelCode;
    private Integer tokensPrompt;
    private Integer tokensCompletion;
    private Integer latencyMs;
    /** 检索元数据（JSON），便于审计 RAG 过程 */
    private String retrievalParamsJson;
    /** normal/warning/blocked */
    private String riskLevel;
    /** 引用依据；messageId 由持久化阶段回填 */
    private List<AiCitationDO> citations;
}
