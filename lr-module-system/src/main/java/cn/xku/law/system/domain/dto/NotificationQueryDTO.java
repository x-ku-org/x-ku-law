package cn.xku.law.system.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends PageParam {
    private String notificationType;
    private String status;
    private Long userId;
}
