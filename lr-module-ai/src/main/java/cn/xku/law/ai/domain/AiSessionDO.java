package cn.xku.law.ai.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** AI 对话会话，对应 lr_ai_session */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_ai_session")
public class AiSessionDO extends BaseDO {

    private Long userId;
    private String sessionTitle;
    /** qa/summary/compare/compliance/drafting */
    private String scenarioType;
    private String modelCode;
    private String status;
    private java.time.LocalDateTime lastMessageTime;
    /** 此前对话的滚动摘要（增量维护） */
    private String contextSummary;
    /** 摘要已覆盖到的最后一条 lr_ai_message.id */
    private Long summaryUptoMessageId;
}
