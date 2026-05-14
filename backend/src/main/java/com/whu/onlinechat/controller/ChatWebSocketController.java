package com.whu.onlinechat.controller;

import com.whu.onlinechat.dto.ChatMessageRequest;
import com.whu.onlinechat.dto.RecallMessageRequest;
import com.whu.onlinechat.service.MessageService;
import com.whu.onlinechat.websocket.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final MessageService messageService;

    @MessageMapping("/chat.private")
    public void privateMessage(UserPrincipal user, @Payload ChatMessageRequest request) {
        messageService.sendPrivate(user.id(), request);
    }

    @MessageMapping("/chat.group")
    public void groupMessage(UserPrincipal user, @Payload ChatMessageRequest request) {
        messageService.sendGroup(user.id(), request);
    }

    @MessageMapping("/chat.recall")
    public void recall(UserPrincipal user, @Payload RecallMessageRequest request) {
        messageService.recall(user.id(), request.messageId());
    }

    @MessageMapping("/read.private")
    public void readPrivate(UserPrincipal user, @Payload ChatMessageRequest request) {
        messageService.readPrivate(user.id(), request.receiverId());
    }

    @MessageMapping("/read.group")
    public void readGroup(UserPrincipal user, @Payload ChatMessageRequest request) {
        messageService.readGroup(user.id(), request.groupId());
    }
}
