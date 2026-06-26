package cn.xku.law.workspace.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeedbackQueryDTO extends PageParam {
    private String feedbackType;
    private String status;
}
