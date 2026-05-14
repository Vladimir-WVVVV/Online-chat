package com.whu.onlinechat.dto;

public record ChatMessageRequest(
    Long receiverId,
    Long groupId,
    String content,
    String messageType,
    Long fileId
) {
}
