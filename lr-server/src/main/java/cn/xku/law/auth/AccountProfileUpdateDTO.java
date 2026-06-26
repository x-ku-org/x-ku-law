package cn.xku.law.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户自助修改资料（PUT /account/profile）。
 * 仅放开下列字段；username/userType/status/tenant 一律由后端忽略。
 * 头像不在此处改，走 POST /account/avatar 上传。字段允许传空串表示清空；email/mobile 用正则兼容空值。
 */
@Data
public class AccountProfileUpdateDTO {

    @Size(max = 64, message = "真实姓名过长")
    private String realName;

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱过长")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Size(max = 16, message = "性别取值过长")
    private String gender;
}
