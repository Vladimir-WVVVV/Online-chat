package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.ChangePasswordRequest;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.UserService;
import com.whu.onlinechat.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResult<UserVO> me(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(userService.getProfile(user.id()));
    }

    @PutMapping("/me")
    public ApiResult<UserVO> updateMe(@AuthenticationPrincipal CurrentUser user,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResult.success(userService.updateProfile(user.id(), request));
    }

    @PostMapping("/avatar")
    public ApiResult<UserVO> uploadAvatar(@AuthenticationPrincipal CurrentUser user,
                                          @RequestPart("file") MultipartFile file) {
        return ApiResult.success(userService.uploadAvatar(user.id(), file));
    }

    @GetMapping("/avatars/{filename}")
    public ResponseEntity<Resource> avatar(@PathVariable String filename) {
        UserService.AvatarFile avatar = userService.loadAvatar(filename);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(avatar.contentType()))
            .body(avatar.resource());
    }

    @PutMapping("/me/password")
    public ApiResult<Void> changePassword(@AuthenticationPrincipal CurrentUser user,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.id(), request);
        return ApiResult.success();
    }

    @GetMapping("/search")
    public ApiResult<java.util.List<UserVO>> search(@AuthenticationPrincipal CurrentUser user,
                                                    @RequestParam(required = false) String keyword) {
        return ApiResult.success(userService.search(user.id(), keyword));
    }
}
