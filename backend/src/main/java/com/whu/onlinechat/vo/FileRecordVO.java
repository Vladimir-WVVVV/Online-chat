package com.whu.onlinechat.vo;

import java.time.LocalDateTime;

public record FileRecordVO(Long id, String originalName, Long fileSize, String mimeType, String downloadUrl,
                           LocalDateTime createTime) {
}
