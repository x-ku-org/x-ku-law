package cn.xku.law.system.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.system.domain.dto.NotificationCreateDTO;
import cn.xku.law.system.domain.dto.NotificationInboxQueryDTO;
import cn.xku.law.system.domain.dto.NotificationQueryDTO;
import cn.xku.law.system.domain.vo.NotificationInboxVO;
import cn.xku.law.system.domain.vo.NotificationVO;
import cn.xku.law.system.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/system/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "后台通知列表（管理员）")
    @PreAuthorize("hasAuthority('notification:list')")
    @GetMapping
    public CommonResult<PageResult<NotificationVO>> page(@Valid NotificationQueryDTO query) {
        return CommonResult.success(notificationService.pageNotifications(query));
    }

    @Operation(summary = "我的通知收件箱（当前用户）")
    @GetMapping("/inbox")
    public CommonResult<PageResult<NotificationInboxVO>> inbox(@Valid NotificationInboxQueryDTO query) {
        return CommonResult.success(notificationService.pageInbox(query));
    }

    @Operation(summary = "发送通知")
    @PreAuthorize("hasAuthority('notification:send')")
    @OperLog(module = "通知管理", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody NotificationCreateDTO dto) {
        return CommonResult.success(notificationService.createNotification(dto));
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public CommonResult<?> markRead(@PathVariable Long id) {
        notificationService.markRead(id, SecurityUtils.getCurrentUserId());
        return CommonResult.success();
    }

    @Operation(summary = "全部通知标记为已读")
    @PutMapping("/inbox/read-all")
    public CommonResult<Long> markAllRead() {
        return CommonResult.success(notificationService.markAllRead(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/inbox/unread-count")
    public CommonResult<Long> unreadCount() {
        return CommonResult.success(notificationService.countUnread(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "删除通知")
    @PreAuthorize("hasAuthority('notification:send')")
    @OperLog(module = "通知管理", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        notificationService.removeNotification(id);
        return CommonResult.success();
    }
}
