package com.whu.onlinechat.ai;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/chat")
    public ApiResult<AiChatResponse> chat(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody AiChatRequest request) {
        return ApiResult.success(aiService.chat(user.id(), request));
    }

    @PostMapping("/summary")
    public ApiResult<AiChatResponse> summary(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody AiChatRequest request) {
        return ApiResult.success(aiService.summary(user.id(), request));
    }

    @PostMapping("/mood")
    public ApiResult<AiChatResponse> mood(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody AiChatRequest request) {
        return ApiResult.success(aiService.mood(user.id(), request));
    }
}
