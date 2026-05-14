package com.whu.onlinechat.websocket;

import com.whu.onlinechat.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof UserPrincipal principal) {
            onlineUserService.online(principal.id(), accessor.getSessionId());
            messagingTemplate.convertAndSend("/topic/online", principal.id());
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        if (event.getUser() instanceof UserPrincipal principal) {
            onlineUserService.offline(principal.id(), event.getSessionId());
            messagingTemplate.convertAndSend("/topic/online", principal.id());
        }
    }
}
