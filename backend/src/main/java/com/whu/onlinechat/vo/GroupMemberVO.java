package com.whu.onlinechat.vo;

import java.time.LocalDateTime;

public record GroupMemberVO(Long userId, String username, String nickname, String avatarUrl, String status,
                            String role, LocalDateTime joinedTime) {
}
