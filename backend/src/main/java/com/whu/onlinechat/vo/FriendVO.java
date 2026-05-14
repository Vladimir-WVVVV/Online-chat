package com.whu.onlinechat.vo;

public record FriendVO(Long id, String username, String nickname, String avatarUrl, String bio, String status,
                       String remark, Long unreadCount) {
}
