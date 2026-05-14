package com.whu.onlinechat.vo;

import java.time.LocalDateTime;

public record NotificationVO(Long id, String type, String content, Boolean read, LocalDateTime createTime) {
}
