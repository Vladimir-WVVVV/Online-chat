package com.whu.onlinechat.dto;

import jakarta.validation.constraints.NotNull;

public record RecallMessageRequest(@NotNull Long messageId) {
}
