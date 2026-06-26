package cn.xku.law.auth;

import cn.xku.law.common.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

@Tag(name = "账号设置", description = "当前登录用户自助管理资料、密码与个性化偏好")
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AvatarUrls avatarUrls;

    @Operation(summary = "我的账号资料", description = "返回当前用户完整资料，供账号概览与资料编辑")
    @GetMapping("/profile")
    public CommonResult<AccountProfileVO> profile() {
        return CommonResult.success(accountService.getProfile());
    }

    @Operation(summary = "修改我的资料", description = "仅 realName/nickname/email/mobile/gender/avatarUrl 生效")
    @PutMapping("/profile")
    public CommonResult<?> updateProfile(@Valid @RequestBody AccountProfileUpdateDTO dto) {
        accountService.updateProfile(dto);
        return CommonResult.success();
    }

    @Operation(summary = "修改密码", description = "校验旧密码后更新；不影响当前会话")
    @PostMapping("/password")
    public CommonResult<?> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        accountService.changePassword(dto);
        return CommonResult.success();
    }

    @Operation(summary = "上传头像", description = "multipart 上传图片，返回更新后的资料（含新头像地址）")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult<AccountProfileVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return CommonResult.success(accountService.uploadAvatar(file));
    }

    @Operation(summary = "读取头像", description = "公开取流端点，供 <img> 直接展示；需携带本服务签发的签名 t，无效或无头像均返回 404")
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<byte[]> avatar(@PathVariable Long userId,
                                         @RequestParam(value = "t", required = false) String token) {
        // 签名校验失败一律按「不存在」处理（返回 404 而非 403），避免据响应码枚举出存在的 userId。
        if (!avatarUrls.verify(userId, token)) {
            return ResponseEntity.notFound().build();
        }
        AccountService.AvatarData avatar = accountService.loadAvatar(userId);
        if (avatar == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(avatar.contentType());
        } catch (Exception e) {
            mediaType = MediaType.IMAGE_PNG;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(avatar.data());
    }

    @Operation(summary = "我的偏好", description = "返回当前用户全部个性化偏好（key → value）")
    @GetMapping("/preferences")
    public CommonResult<Map<String, String>> preferences() {
        return CommonResult.success(accountService.getPreferences());
    }

    @Operation(summary = "保存偏好", description = "按 key upsert 个性化偏好")
    @PutMapping("/preferences")
    public CommonResult<?> savePreferences(@RequestBody Map<String, String> kv) {
        accountService.savePreferences(kv);
        return CommonResult.success();
    }
}
