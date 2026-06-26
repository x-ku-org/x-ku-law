package cn.xku.law.auth;

import cn.xku.law.common.constant.SecurityConstants;
import cn.xku.law.common.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "登录、登出、刷新 Token")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "tenantCode + 用户名/密码，返回 access_token 与 refresh_token")
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return CommonResult.success(authService.login(request, ip, userAgent));
    }

    @Operation(summary = "自助注册", description = "用户名/密码/手机号，注册成功后自动登录并返回 access_token 与 refresh_token")
    @PostMapping("/register")
    public CommonResult<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return CommonResult.success(authService.register(request, ip, userAgent));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public CommonResult<String> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return CommonResult.success(authService.refresh(refreshToken));
    }

    @Operation(summary = "当前登录用户", description = "返回当前用户资料、角色码与权限码，供前端做管理员门禁与个性化展示")
    @GetMapping("/me")
    public CommonResult<CurrentUserVO> me() {
        return CommonResult.success(authService.currentUser());
    }

    @Operation(summary = "登出", description = "删除 Redis 会话，使 Token 立即失效")
    @PostMapping("/logout")
    public CommonResult<?> logout(@RequestHeader(SecurityConstants.TOKEN_HEADER) String authHeader) {
        String token = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        authService.logout(token);
        return CommonResult.success();
    }
}
