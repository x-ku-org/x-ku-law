package cn.xku.law.ai.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** AI 消息，对应 lr_ai_message */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_ai_message")
public class AiMessageDO extends BaseDO {

    private Long sessionId;
    private Long userId;
    /** user/assistant/system */
    private String messageRole;
    private String questionText;
    private String answerText;
    private Integer tokensPrompt;
    private Integer tokensCompletion;
    private Integer latencyMs;
    private String modelCode;
    private String retrievalParamsJson;
    /** normal/warning/blocked */
    private String riskLevel;
}
