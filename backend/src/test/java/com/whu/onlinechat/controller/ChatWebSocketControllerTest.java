package com.whu.onlinechat.controller;

import com.whu.onlinechat.dto.ChatMessageRequest;
import com.whu.onlinechat.dto.RecallMessageRequest;
import com.whu.onlinechat.service.MessageService;
import com.whu.onlinechat.websocket.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatWebSocketController 单元测试")
class ChatWebSocketControllerTest {

    @Mock private MessageService messageService;
    @InjectMocks private ChatWebSocketController controller;
    private final UserPrincipal alice = new UserPrincipal(1L, "alice", "USER");

    @Nested
    @DisplayName("私聊消息")
    class PrivateMessage {

        @Test
        @DisplayName("正常发送私聊消息")
        void shouldSendPrivateMessage() {
            var req = new ChatMessageRequest(2L, null, "Hello", "TEXT", null);

            controller.privateMessage(alice, req);

            verify(messageService).sendPrivate(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("群聊消息")
    class GroupMessage {

        @Test
        @DisplayName("正常发送群聊消息")
        void shouldSendGroupMessage() {
            var req = new ChatMessageRequest(null, 100L, "Hello", "TEXT", null);

            controller.groupMessage(alice, req);

            verify(messageService).sendGroup(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("撤回消息")
    class Recall {

        @Test
        @DisplayName("正常撤回消息")
        void shouldRecall() {
            var req = new RecallMessageRequest(10L);

            controller.recall(alice, req);

            verify(messageService).recall(1L, 10L);
        }
    }

    @Nested
    @DisplayName("标记私聊已读")
    class ReadPrivate {

        @Test
        @DisplayName("正常标记已读")
        void shouldReadPrivate() {
            var req = new ChatMessageRequest(2L, null, null, null, null);

            controller.readPrivate(alice, req);

            verify(messageService).readPrivate(1L, 2L);
        }
    }

    @Nested
    @DisplayName("标记群聊已读")
    class ReadGroup {

        @Test
        @DisplayName("正常标记已读")
        void shouldReadGroup() {
            var req = new ChatMessageRequest(null, 100L, null, null, null);

            controller.readGroup(alice, req);

            verify(messageService).readGroup(1L, 100L);
        }
    }
}
