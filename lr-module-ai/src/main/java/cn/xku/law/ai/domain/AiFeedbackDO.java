package cn.xku.law.ai.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** AI 回答反馈（点赞/纠错/转人工），对应 lr_ai_feedback */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_ai_feedback")
public class AiFeedbackDO extends BaseDO {

    private Long messageId;
    private Long userId;
    /** like/dislike/error/hallucination/missing_citation/escalate */
    private String feedbackType;
    /** 评分 1-5（可空） */
    private Integer rating;
    private String feedbackContent;
    /** pending/processing/resolved/closed */
    private String handledStatus;
    private Long handledUserId;
    private LocalDateTime handledTime;
}
