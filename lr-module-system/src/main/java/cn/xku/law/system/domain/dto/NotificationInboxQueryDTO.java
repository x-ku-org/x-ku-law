package cn.xku.law.system.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationInboxQueryDTO extends PageParam {
    /** 已读状态过滤: unread / read，不传则返回全部 */
    private String readStatus;
}
