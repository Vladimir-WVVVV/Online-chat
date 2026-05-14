package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.NotificationService;
import com.whu.onlinechat.vo.NotificationVO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResult<List<NotificationVO>> list(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(notificationService.list(user.id()));
    }

    @GetMapping("/unread-count")
    public ApiResult<Map<String, Long>> unread(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(Map.of("count", notificationService.unreadCount(user.id())));
    }

    @PostMapping("/{id}/read")
    public ApiResult<Void> read(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        notificationService.read(user.id(), id);
        return ApiResult.success();
    }

    @PostMapping("/read-all")
    public ApiResult<Void> readAll(@AuthenticationPrincipal CurrentUser user) {
        notificationService.readAll(user.id());
        return ApiResult.success();
    }
}
