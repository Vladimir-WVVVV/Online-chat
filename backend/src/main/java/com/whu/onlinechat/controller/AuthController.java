package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.LoginRequest;
import com.whu.onlinechat.dto.RegisterRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.AuthService;
import com.whu.onlinechat.vo.LoginVO;
import com.whu.onlinechat.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResult<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResult<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.success();
    }

    @GetMapping("/me")
    public ApiResult<UserVO> me(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(authService.me(user.id()));
    }
}

