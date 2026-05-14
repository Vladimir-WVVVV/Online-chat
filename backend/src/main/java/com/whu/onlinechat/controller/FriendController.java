package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.FriendRequestCreateRequest;
import com.whu.onlinechat.dto.RemarkRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.FriendService;
import com.whu.onlinechat.vo.FriendRequestVO;
import com.whu.onlinechat.vo.FriendVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @GetMapping
    public ApiResult<List<FriendVO>> list(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(friendService.list(user.id()));
    }

    @PostMapping("/requests")
    public ApiResult<Void> request(@AuthenticationPrincipal CurrentUser user,
                                   @Valid @RequestBody FriendRequestCreateRequest request) {
        friendService.request(user.id(), request);
        return ApiResult.success();
    }

    @GetMapping("/requests/received")
    public ApiResult<List<FriendRequestVO>> received(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(friendService.received(user.id()));
    }

    @GetMapping("/requests/sent")
    public ApiResult<List<FriendRequestVO>> sent(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(friendService.sent(user.id()));
    }

    @PostMapping("/requests/{id}/accept")
    public ApiResult<Void> accept(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        friendService.accept(user.id(), id);
        return ApiResult.success();
    }

    @PostMapping("/requests/{id}/reject")
    public ApiResult<Void> reject(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        friendService.reject(user.id(), id);
        return ApiResult.success();
    }

    @PutMapping("/{friendId}/remark")
    public ApiResult<Void> remark(@AuthenticationPrincipal CurrentUser user, @PathVariable Long friendId,
                                  @RequestBody RemarkRequest request) {
        friendService.remark(user.id(), friendId, request);
        return ApiResult.success();
    }

    @DeleteMapping("/{friendId}")
    public ApiResult<Void> delete(@AuthenticationPrincipal CurrentUser user, @PathVariable Long friendId) {
        friendService.delete(user.id(), friendId);
        return ApiResult.success();
    }
}
