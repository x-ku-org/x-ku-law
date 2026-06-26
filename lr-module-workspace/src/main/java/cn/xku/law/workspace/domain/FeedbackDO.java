package cn.xku.law.workspace.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 用户反馈纠错，对应 lr_feedback */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_feedback")
public class FeedbackDO extends BaseDO {

    private Long userId;
    /** data_error/search_error/ai_error/function/suggestion */
    private String feedbackType;
    private String refType;
    private Long refId;
    private String content;
    /** pending/processing/resolved/closed */
    private String status;
    private Long handlerUserId;
    private LocalDateTime handledTime;
    private String resultDesc;
}
