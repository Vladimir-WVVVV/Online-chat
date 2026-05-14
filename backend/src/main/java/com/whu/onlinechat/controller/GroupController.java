package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.CreateGroupRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.GroupService;
import com.whu.onlinechat.vo.GroupMemberVO;
import com.whu.onlinechat.vo.GroupVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ApiResult<GroupVO> create(@AuthenticationPrincipal CurrentUser user,
                                     @Valid @RequestBody CreateGroupRequest request) {
        return ApiResult.success(groupService.create(user.id(), request));
    }

    @GetMapping
    public ApiResult<List<GroupVO>> mine(@AuthenticationPrincipal CurrentUser user) {
        return ApiResult.success(groupService.mine(user.id()));
    }

    @GetMapping("/{id}")
    public ApiResult<GroupVO> detail(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return ApiResult.success(groupService.detail(user.id(), id));
    }

    @PostMapping("/{id}/join")
    public ApiResult<Void> join(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        groupService.join(user.id(), id);
        return ApiResult.success();
    }

    @PostMapping("/{id}/leave")
    public ApiResult<Void> leave(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        groupService.leave(user.id(), id);
        return ApiResult.success();
    }

    @GetMapping("/{id}/members")
    public ApiResult<List<GroupMemberVO>> members(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return ApiResult.success(groupService.members(user.id(), id));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ApiResult<Void> remove(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id,
                                  @PathVariable Long userId) {
        groupService.removeMember(user.id(), id, userId);
        return ApiResult.success();
    }
}
