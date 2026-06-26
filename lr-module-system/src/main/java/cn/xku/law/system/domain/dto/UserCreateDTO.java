package cn.xku.law.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank private String username;
    @NotBlank private String password;
    private String realName;
    private String nickname;
    private String mobile;
    private String email;
    private String gender;
    private String userType;
    private String status;
}
