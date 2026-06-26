package cn.xku.law.workspace.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 反馈附件，对应 lr_feedback_attachment */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_feedback_attachment")
public class FeedbackAttachmentDO extends BaseDO {

    private Long feedbackId;
    private Long fileId;
}
