package com.whu.onlinechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotBlank @Size(max = 80) String name,
    String avatarUrl,
    String description
) {
}
