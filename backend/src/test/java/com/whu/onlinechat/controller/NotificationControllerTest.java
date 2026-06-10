package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.NotificationService;
import com.whu.onlinechat.vo.NotificationVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController 单元测试")
class NotificationControllerTest {

    @Mock private NotificationService notificationService;
    @InjectMocks private NotificationController controller;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("通知列表")
    class ListNotifications {

        @Test
        @DisplayName("正常返回通知列表")
        void shouldReturnList() {
            var vo = new NotificationVO(1L, "FRIEND_REQUEST", "用户 2 请求添加你为好友", false, LocalDateTime.now());
            when(notificationService.list(1L)).thenReturn(java.util.List.of(vo));

            var result = controller.list(alice);

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.data()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("未读数量")
    class UnreadCount {

        @Test
        @DisplayName("返回未读数量")
        void shouldReturnUnreadCount() {
            when(notificationService.unreadCount(1L)).thenReturn(5L);

            var result = controller.unread(alice);

            assertThat(result.data()).containsEntry("count", 5L);
        }
    }

    @Nested
    @DisplayName("标记已读")
    class Read {

        @Test
        @DisplayName("标记单个通知已读")
        void shouldReadSingle() {
            var result = controller.read(alice, 10L);

            assertThat(result.code()).isEqualTo(0);
            verify(notificationService).read(1L, 10L);
        }
    }

    @Nested
    @DisplayName("全部已读")
    class ReadAll {

        @Test
        @DisplayName("标记全部通知已读")
        void shouldReadAll() {
            var result = controller.readAll(alice);

            assertThat(result.code()).isEqualTo(0);
            verify(notificationService).readAll(1L);
        }
    }
}
