package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.entity.AdminLog;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.AdminService;
import com.whu.onlinechat.vo.AdminMetricsVO;
import com.whu.onlinechat.vo.UserVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResult<List<UserVO>> users() {
        return ApiResult.success(adminService.users());
    }

    @PostMapping("/users/{id}/ban")
    public ApiResult<Void> ban(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        adminService.ban(user.id(), id);
        return ApiResult.success();
    }

    @PostMapping("/users/{id}/unban")
    public ApiResult<Void> unban(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        adminService.unban(user.id(), id);
        return ApiResult.success();
    }

    @GetMapping("/metrics")
    public ApiResult<AdminMetricsVO> metrics() {
        return ApiResult.success(adminService.metrics());
    }

    @GetMapping("/logs")
    public ApiResult<List<AdminLog>> logs() {
        return ApiResult.success(adminService.logs());
    }
}
