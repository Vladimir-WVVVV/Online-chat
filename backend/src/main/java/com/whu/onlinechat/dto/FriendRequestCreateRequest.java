package com.whu.onlinechat.dto;

import jakarta.validation.constraints.NotNull;

public record FriendRequestCreateRequest(@NotNull Long toUserId, String message) {
}
