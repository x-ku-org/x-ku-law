package cn.xku.law.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationCreateDTO {
    private String notificationType;
    private String templateCode;
    @NotBlank private String title;
    @NotBlank private String content;
    private String refType;
    private Long refId;
    /** single/role/tenant/all */
    private String sendScope;
    /** sendScope=single 时的目标用户ID */
    private Long targetUserId;
    /** sendScope=role 时的目标角色ID */
    private Long targetRoleId;
    /** station/email/sms */
    private String channel;
}
