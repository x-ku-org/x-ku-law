package cn.xku.law.auth;

import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.system.domain.TenantDO;
import cn.xku.law.system.domain.UserDO;
import cn.xku.law.system.mapper.TenantMapper;
import cn.xku.law.system.mapper.UserMapper;
import cn.xku.law.system.mapper.UserRoleMapper;
import cn.xku.law.system.service.UserPreferenceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_AVATAR_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPreferenceService preferenceService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageClient fileStorageClient;
    private final AvatarUrls avatarUrls;

    @Override
    public AccountProfileVO getProfile() {
        Long userId = currentUserId();
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        TenantDO tenant = tenantMapper.selectById(user.getTenantId());

        List<String> roles;
        try {
            roles = userRoleMapper.selectRoleCodes(userId, user.getTenantId());
        } catch (Exception e) {
            log.warn("[AccountService] 加载角色码失败，降级为空角色: userId={}", userId, e);
            roles = List.of();
        }
        if (roles == null) roles = List.of();

        return new AccountProfileVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getNickname(),
                user.getEmail(),
                user.getMobile(),
                user.getGender(),
                avatarUrls.publicPath(user),
                user.getUserType(),
                tenant != null ? tenant.getTenantCode() : null,
                tenant != null ? tenant.getTenantName() : null,
                roles,
                user.getLastLoginTime(),
                user.getLastLoginIp(),
                user.getPasswordUpdateTime(),
                user.getCreateTime());
    }

    @Override
    public void updateProfile(AccountProfileUpdateDTO dto) {
        Long userId = currentUserId();
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        }

        // 邮箱/手机号在本租户内唯一（排除自身）；租户作用域由多租户插件按当前会话注入。
        if (StringUtils.hasText(dto.getEmail())) {
            Long emailDup = userMapper.selectCount(new LambdaQueryWrapper<UserDO>()
                    .eq(UserDO::getEmail, dto.getEmail())
                    .ne(UserDO::getId, userId));
            if (emailDup != null && emailDup > 0) {
                throw new AppException(ErrorCode.PARAM_ERROR, "该邮箱已被使用");
            }
        }
        if (StringUtils.hasText(dto.getMobile())) {
            Long mobileDup = userMapper.selectCount(new LambdaQueryWrapper<UserDO>()
                    .eq(UserDO::getMobile, dto.getMobile())
                    .ne(UserDO::getId, userId));
            if (mobileDup != null && mobileDup > 0) {
                throw new AppException(ErrorCode.AUTH_MOBILE_EXISTS);
            }
        }

        // 仅在字段被显式传入时更新（null=未提交，空串=清空）。email/mobile 有租户内唯一键，
        // 清空必须落 NULL 而非空串，否则多个用户的 "" 会撞 uk_user_email/uk_user_mobile；
        // 又因 updateById 默认跳过 null 字段，这里用 UpdateWrapper 显式 set 才能把列写成 NULL。
        LambdaUpdateWrapper<UserDO> update = new LambdaUpdateWrapper<UserDO>().eq(UserDO::getId, userId);
        boolean dirty = false;
        if (dto.getRealName() != null) { update.set(UserDO::getRealName, dto.getRealName()); dirty = true; }
        if (dto.getNickname() != null) { update.set(UserDO::getNickname, dto.getNickname()); dirty = true; }
        if (dto.getEmail() != null) { update.set(UserDO::getEmail, blankToNull(dto.getEmail())); dirty = true; }
        if (dto.getMobile() != null) { update.set(UserDO::getMobile, blankToNull(dto.getMobile())); dirty = true; }
        if (dto.getGender() != null) { update.set(UserDO::getGender, dto.getGender()); dirty = true; }
        if (dirty) {
            userMapper.update(null, update);
        }
    }

    /** 空白（null/空串/纯空格）归一为 null，用于带唯一键的可空列清空时落 NULL 而非空串。 */
    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }

    @Override
    public AccountProfileVO uploadAvatar(MultipartFile file) {
        Long userId = currentUserId();
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.SYS_FILE_NOT_UPLOADED);
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new AppException(ErrorCode.SYS_FILE_TOO_LARGE, "头像不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.SYS_FILE_TYPE_NOT_ALLOWED, "仅支持 JPG/PNG/WEBP/GIF 图片");
        }
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        }

        // 稳定对象 key（按租户/用户固定，重传覆盖），避免堆积历史头像。
        String objectKey = "avatar/" + user.getTenantId() + "/" + userId;
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new AppException(ErrorCode.SYS_FILE_UPLOAD_FAIL, "头像读取失败");
        }
        fileStorageClient.upload(objectKey, data, contentType);

        user.setAvatarUrl(objectKey);
        userMapper.updateById(user);
        return getProfile();
    }

    @Override
    public AvatarData loadAvatar(Long userId) {
        if (userId == null) {
            return null;
        }
        // 该接口对外公开（供 <img> 直接取流），无登录上下文：绕过租户插件按 ID 取用户。
        UserDO user = userMapper.selectActiveByIdIgnoreTenant(userId);
        if (user == null || !StringUtils.hasText(user.getAvatarUrl())) {
            return null;
        }
        String objectKey = user.getAvatarUrl();
        byte[] data = fileStorageClient.download(objectKey);
        if (data == null || data.length == 0) {
            return null;
        }
        String contentType = "image/png";
        try {
            var stat = fileStorageClient.statObject(objectKey);
            if (stat != null && StringUtils.hasText(stat.contentType())) {
                contentType = stat.contentType();
            }
        } catch (Exception ignored) {
            // statObject 不可用时退回默认 content-type，不阻断取流
        }
        return new AvatarData(data, contentType);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = currentUserId();
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new AppException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTH_PASSWORD_ERROR, "当前密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public Map<String, String> getPreferences() {
        return preferenceService.getPreferences(currentUserId());
    }

    @Override
    public void savePreferences(Map<String, String> kv) {
        preferenceService.savePreferences(currentUserId(), kv);
    }

    private Long currentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
