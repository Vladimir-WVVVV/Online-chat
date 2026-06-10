package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.Notification;
import com.whu.onlinechat.mapper.NotificationMapper;
import com.whu.onlinechat.vo.NotificationVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 单元测试")
class NotificationServiceTest {

    @Mock private NotificationMapper notificationMapper;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private NotificationService notificationService;

    @Nested
    @DisplayName("创建通知")
    class Create {

        @Test
        @DisplayName("正常创建通知并推送")
        void shouldCreateAndPush() {
            NotificationVO result = notificationService.create(1L, "FRIEND_REQUEST", "用户 2 请求添加你为好友");

            assertThat(result.type()).isEqualTo("FRIEND_REQUEST");
            assertThat(result.read()).isFalse();
            verify(notificationMapper).insert(any(Notification.class));
            verify(messagingTemplate).convertAndSendToUser(any(), any(), any(NotificationVO.class));
        }
    }

    @Nested
    @DisplayName("未读数量")
    class UnreadCount {

        @Test
        @DisplayName("返回未读通知数量")
        void shouldReturnUnreadCount() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            Long count = notificationService.unreadCount(1L);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("标记已读")
    class Read {

        @Test
        @DisplayName("标记单个通知为已读")
        void shouldReadSingle() {
            when(notificationMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            notificationService.read(1L, 10L);

            verify(notificationMapper).update(any(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("通知不存在时抛异常")
        void shouldThrowWhenNotFound() {
            when(notificationMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            assertThatThrownBy(() -> notificationService.read(1L, 999L))
                .isInstanceOf(BizException.class)
                .hasMessage("通知不存在");
        }
    }

    @Nested
    @DisplayName("全部已读")
    class ReadAll {

        @Test
        @DisplayName("标记所有通知为已读")
        void shouldReadAll() {
            notificationService.readAll(1L);

            verify(notificationMapper).update(any(), any(LambdaUpdateWrapper.class));
        }
    }
}
