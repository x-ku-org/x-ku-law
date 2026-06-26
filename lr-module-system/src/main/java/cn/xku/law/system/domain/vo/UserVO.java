package cn.xku.law.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String mobile;
    private String email;
    private String avatarUrl;
    private String gender;
    private String userType;
    private String status;
    private LocalDateTime lastLoginTime;
    private Long tenantId;
    private LocalDateTime createTime;
}
