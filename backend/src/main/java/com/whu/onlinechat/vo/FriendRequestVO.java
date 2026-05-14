package com.whu.onlinechat.vo;

import java.time.LocalDateTime;

public record FriendRequestVO(Long id, Long fromUserId, String fromUsername, String fromNickname, Long toUserId,
                              String toUsername, String toNickname, String status, String message,
                              LocalDateTime createTime) {
}
