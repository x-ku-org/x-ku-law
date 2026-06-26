package cn.xku.law.auth;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** 当前登录用户的自助账号管理（资料、密码、头像、个性化偏好）。 */
public interface AccountService {

    /** 当前用户完整资料，供账号设置页概览与资料编辑。 */
    AccountProfileVO getProfile();

    /** 自助修改资料；仅 realName/nickname/email/mobile/gender 生效（头像走上传）。 */
    void updateProfile(AccountProfileUpdateDTO dto);

    /** 上传并设置当前用户头像，返回更新后的资料（含新的头像地址）。 */
    AccountProfileVO uploadAvatar(MultipartFile file);

    /** 按用户 ID 读取头像二进制（供 GET /account/avatar/{userId} 流式输出）；无头像返回 null。 */
    AvatarData loadAvatar(Long userId);

    /** 头像二进制与其内容类型。 */
    record AvatarData(byte[] data, String contentType) {}

    /** 自助修改密码，校验旧密码。 */
    void changePassword(ChangePasswordDTO dto);

    /** 当前用户全部偏好（key → value）。 */
    Map<String, String> getPreferences();

    /** 按 key upsert 偏好。 */
    void savePreferences(Map<String, String> kv);
}
