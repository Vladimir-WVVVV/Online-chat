package com.whu.onlinechat.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 50) String nickname,
    @Size(max = 500) String avatarUrl,
    @Size(max = 500) String bio
) {
}

