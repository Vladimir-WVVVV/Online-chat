package com.whu.onlinechat.vo;

import java.time.LocalDateTime;

public record GroupVO(Long id, String name, Long ownerId, String ownerName, String avatarUrl, String description,
                      Long memberCount, Long unreadCount, LocalDateTime createTime) {
}
