package com.whu.onlinechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateGroupRequest(
    @NotBlank(message = "群名不能为空") @Size(max = 80, message = "群名不能超过 80 个字符") String name,
    String avatarUrl,
    @Size(max = 500, message = "群聊简介不能超过 500 个字符") String description,
    List<Long> memberIds
) {
}
