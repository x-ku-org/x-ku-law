package cn.xku.law.system.domain.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String password;
    private String realName;
    private String nickname;
    private String mobile;
    private String email;
    private String gender;
    private String userType;
    private String status;
}
