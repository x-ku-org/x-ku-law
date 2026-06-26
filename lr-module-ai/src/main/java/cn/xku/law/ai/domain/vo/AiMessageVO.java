package cn.xku.law.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiMessageVO {
    private Long id;
    private Long sessionId;
    private Long userId;
    private Integer tokensPrompt;
    private Integer tokensCompletion;
    private Integer latencyMs;
    private String modelCode;
    private String riskLevel;
    private LocalDateTime createTime;

    /** 角色（由 DO.messageRole 映射）。 */
    private String role;
    /** 正文（user 取 questionText，assistant 取 answerText）。 */
    private String content;
    /** 引用依据列表 */
    private List<AiCitationVO> citations;
    /** 当前用户是否已对该回答点赞（重新进入会话时恢复「已赞」状态）。 */
    private Boolean liked;
}
