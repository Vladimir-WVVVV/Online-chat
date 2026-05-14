package com.whu.onlinechat.vo;

import com.whu.onlinechat.entity.Message;
import java.time.LocalDateTime;

public record MessageVO(Long messageId, String conversationType, Long senderId, String senderName, Long receiverId,
                        Long groupId, String content, String messageType, Long fileId, Boolean recalled,
                        LocalDateTime createTime) {
    public static MessageVO of(Message message, String senderName) {
        return new MessageVO(
            message.getId(),
            message.getConversationType(),
            message.getSenderId(),
            senderName,
            message.getReceiverId(),
            message.getGroupId(),
            message.getContent(),
            message.getMessageType(),
            message.getFileId(),
            message.getRecalled() != null && message.getRecalled() == 1,
            message.getCreateTime()
        );
    }
}
