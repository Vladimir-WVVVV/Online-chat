package com.whu.onlinechat.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiChatRequest(
    @NotBlank String conversationType,
    @NotNull Long targetId,
    String agentType,
    String content
) {
}
