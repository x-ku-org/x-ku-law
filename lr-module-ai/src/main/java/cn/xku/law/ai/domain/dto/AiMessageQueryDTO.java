package cn.xku.law.ai.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiMessageQueryDTO extends PageParam {
    private Long sessionId;
}
