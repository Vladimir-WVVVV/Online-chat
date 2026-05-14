package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.MessageService;
import com.whu.onlinechat.vo.MessageVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/private/{friendId}")
    public ApiResult<List<MessageVO>> privateHistory(@AuthenticationPrincipal CurrentUser user, @PathVariable Long friendId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String keyword) {
        return ApiResult.success(messageService.privateHistory(user.id(), friendId, page, size, keyword));
    }

    @GetMapping("/group/{groupId}")
    public ApiResult<List<MessageVO>> groupHistory(@AuthenticationPrincipal CurrentUser user, @PathVariable Long groupId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String keyword) {
        return ApiResult.success(messageService.groupHistory(user.id(), groupId, page, size, keyword));
    }

    @GetMapping("/search")
    public ApiResult<List<MessageVO>> search(@AuthenticationPrincipal CurrentUser user,
                                             @RequestParam(required = false) String keyword) {
        return ApiResult.success(messageService.search(user.id(), keyword));
    }

    @PostMapping("/{messageId}/recall")
    public ApiResult<MessageVO> recall(@AuthenticationPrincipal CurrentUser user, @PathVariable Long messageId) {
        return ApiResult.success(messageService.recall(user.id(), messageId));
    }

    @PostMapping("/read/private/{friendId}")
    public ApiResult<Void> readPrivate(@AuthenticationPrincipal CurrentUser user, @PathVariable Long friendId) {
        messageService.readPrivate(user.id(), friendId);
        return ApiResult.success();
    }

    @PostMapping("/read/group/{groupId}")
    public ApiResult<Void> readGroup(@AuthenticationPrincipal CurrentUser user, @PathVariable Long groupId) {
        messageService.readGroup(user.id(), groupId);
        return ApiResult.success();
    }
}
