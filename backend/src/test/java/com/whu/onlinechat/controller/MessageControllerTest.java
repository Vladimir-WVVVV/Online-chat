package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.MessageService;
import com.whu.onlinechat.vo.MessageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController 单元测试")
class MessageControllerTest {

    @Mock private MessageService messageService;
    @InjectMocks private MessageController messageController;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("私聊历史")
    class PrivateHistory {

        @Test
        @DisplayName("正常返回私聊历史")
        void shouldReturnPrivateHistory() {
            MessageVO msg = new MessageVO(1L, "PRIVATE", 1L, "Alice", 2L, null,
                "Hello", "TEXT", null, false, LocalDateTime.now());
            when(messageService.privateHistory(eq(1L), eq(2L), eq(1), eq(20), isNull()))
                .thenReturn(List.of(msg));

            ApiResult<List<MessageVO>> result = messageController.privateHistory(alice, 2L, 1, 20, null);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).content()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("带关键词搜索私聊历史")
        void shouldSearchPrivateHistory() {
            when(messageService.privateHistory(1L, 2L, 1, 20, "hello"))
                .thenReturn(List.of());

            ApiResult<List<MessageVO>> result = messageController.privateHistory(alice, 2L, 1, 20, "hello");

            assertThat(result.data()).isEmpty();
        }
    }

    @Nested
    @DisplayName("群聊历史")
    class GroupHistory {

        @Test
        @DisplayName("正常返回群聊历史")
        void shouldReturnGroupHistory() {
            when(messageService.groupHistory(1L, 100L, 1, 20, null)).thenReturn(List.of());

            ApiResult<List<MessageVO>> result = messageController.groupHistory(alice, 100L, 1, 20, null);

            assertThat(result.code()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("搜索消息")
    class Search {

        @Test
        @DisplayName("正常搜索消息")
        void shouldSearchMessages() {
            when(messageService.search(1L, "keyword")).thenReturn(List.of());

            ApiResult<List<MessageVO>> result = messageController.search(alice, "keyword");

            assertThat(result.code()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("撤回消息")
    class Recall {

        @Test
        @DisplayName("正常撤回消息")
        void shouldRecall() {
            MessageVO msg = new MessageVO(1L, "PRIVATE", 1L, "Alice", 2L, null,
                "消息已撤回", "TEXT", null, true, LocalDateTime.now());
            when(messageService.recall(1L, 10L)).thenReturn(msg);

            ApiResult<MessageVO> result = messageController.recall(alice, 10L);

            assertThat(result.data().content()).isEqualTo("消息已撤回");
            assertThat(result.data().recalled()).isTrue();
        }
    }

    @Nested
    @DisplayName("标记私聊已读")
    class ReadPrivate {

        @Test
        @DisplayName("正常标记已读")
        void shouldReadPrivate() {
            ApiResult<Void> result = messageController.readPrivate(alice, 2L);

            assertThat(result.code()).isEqualTo(0);
            verify(messageService).readPrivate(1L, 2L);
        }
    }

    @Nested
    @DisplayName("标记群聊已读")
    class ReadGroup {

        @Test
        @DisplayName("正常标记已读")
        void shouldReadGroup() {
            ApiResult<Void> result = messageController.readGroup(alice, 100L);

            assertThat(result.code()).isEqualTo(0);
            verify(messageService).readGroup(1L, 100L);
        }
    }
}
