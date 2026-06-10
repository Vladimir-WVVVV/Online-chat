package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.ChatMessageRequest;
import com.whu.onlinechat.entity.Message;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.MessageMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.MessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 单元测试")
class MessageServiceTest {

    @Mock private MessageMapper messageMapper;
    @Mock private UserMapper userMapper;
    @Mock private FriendService friendService;
    @Mock private GroupService groupService;
    @Mock private UserService userService;
    @Mock private NotificationService notificationService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private MessageService messageService;

    private User alice, bob;

    @BeforeEach
    void setUp() {
        alice = new User(); alice.setId(1L); alice.setNickname("Alice");
        bob = new User(); bob.setId(2L); bob.setNickname("Bob");
    }

    @Nested
    @DisplayName("sendPrivate")
    class SendPrivate {

        @Test
        @DisplayName("正常发送私聊消息")
        void shouldSendPrivateMessage() {
            ChatMessageRequest req = new ChatMessageRequest(2L, null, "Hello", "TEXT", null);
            when(friendService.isFriend(1L, 2L)).thenReturn(true);
            when(userMapper.selectById(1L)).thenReturn(alice);

            MessageVO result = messageService.sendPrivate(1L, req);

            assertThat(result.senderId()).isEqualTo(1L);
            verify(messageMapper).insert(any(Message.class));
            verify(notificationService).create(eq(2L), eq("PRIVATE_MESSAGE"), any());
        }

        @Test
        @DisplayName("非好友无法发私聊")
        void shouldThrowWhenNotFriend() {
            ChatMessageRequest req = new ChatMessageRequest(2L, null, "Hello", "TEXT", null);
            when(friendService.isFriend(1L, 2L)).thenReturn(false);

            assertThatThrownBy(() -> messageService.sendPrivate(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("只能给好友发送私聊消息");
        }

        @Test
        @DisplayName("接收人为空时抛异常")
        void shouldThrowWhenReceiverNull() {
            ChatMessageRequest req = new ChatMessageRequest(null, null, "Hello", "TEXT", null);

            assertThatThrownBy(() -> messageService.sendPrivate(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("接收人不能为空");
        }
    }

    @Nested
    @DisplayName("sendGroup")
    class SendGroup {

        @Test
        @DisplayName("正常发送群聊消息")
        void shouldSendGroupMessage() {
            ChatMessageRequest req = new ChatMessageRequest(null, 100L, "Hello group", "TEXT", null);
            when(userMapper.selectById(1L)).thenReturn(alice);
            when(groupService.memberIds(100L)).thenReturn(List.of(1L, 2L, 3L));

            MessageVO result = messageService.sendGroup(1L, req);

            assertThat(result.conversationType()).isEqualTo("GROUP");
            verify(messageMapper).insert(any(Message.class));
        }

        @Test
        @DisplayName("群聊ID为空时抛异常")
        void shouldThrowWhenGroupNull() {
            ChatMessageRequest req = new ChatMessageRequest(null, null, "Hello", "TEXT", null);

            assertThatThrownBy(() -> messageService.sendGroup(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("群聊不能为空");
        }
    }

    @Nested
    @DisplayName("recall")
    class Recall {

        @Test
        @DisplayName("正常撤回自己的消息(2分钟内)")
        void shouldRecallOwnMessage() {
            Message msg = new Message();
            msg.setId(10L);
            msg.setSenderId(1L);
            msg.setConversationType("PRIVATE");
            msg.setReceiverId(2L);
            msg.setContent("old content");
            msg.setCreateTime(LocalDateTime.now());
            msg.setRecalled(0);
            when(messageMapper.selectById(10L)).thenReturn(msg);
            when(userMapper.selectById(1L)).thenReturn(alice);
            when(messageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            MessageVO result = messageService.recall(1L, 10L);

            assertThat(result.content()).isEqualTo("消息已撤回");
        }

        @Test
        @DisplayName("撤回他人消息时抛异常")
        void shouldThrowWhenNotSender() {
            Message msg = new Message();
            msg.setId(10L);
            msg.setSenderId(2L); // sent by bob
            msg.setCreateTime(LocalDateTime.now());
            when(messageMapper.selectById(10L)).thenReturn(msg);

            assertThatThrownBy(() -> messageService.recall(1L, 10L))
                .isInstanceOf(BizException.class)
                .hasMessage("消息不存在或无权撤回");
        }

        @Test
        @DisplayName("撤回过期消息时抛异常")
        void shouldThrowWhenExpired() {
            Message msg = new Message();
            msg.setId(10L);
            msg.setSenderId(1L);
            msg.setCreateTime(LocalDateTime.now().minusMinutes(5));
            when(messageMapper.selectById(10L)).thenReturn(msg);

            assertThatThrownBy(() -> messageService.recall(1L, 10L))
                .isInstanceOf(BizException.class)
                .hasMessage("只能撤回 2 分钟内的消息");
        }

        @Test
        @DisplayName("消息不存在时抛异常")
        void shouldThrowWhenMessageNotFound() {
            when(messageMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> messageService.recall(1L, 999L))
                .isInstanceOf(BizException.class);
        }
    }

    @Nested
    @DisplayName("历史消息")
    class History {

        @Test
        @DisplayName("查询私聊历史记录")
        void shouldReturnPrivateHistory() {
            Message msg = new Message();
            msg.setId(1L);
            msg.setConversationType("PRIVATE");
            msg.setSenderId(1L);
            msg.setReceiverId(2L);
            msg.setContent("Hello");
            msg.setRecalled(0);
            when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(msg));
            when(userMapper.selectById(1L)).thenReturn(alice);

            List<MessageVO> result = messageService.privateHistory(1L, 2L, 1, 20, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("查询群聊历史记录")
        void shouldReturnGroupHistory() {
            Message msg = new Message();
            msg.setId(1L);
            msg.setConversationType("GROUP");
            msg.setGroupId(100L);
            msg.setSenderId(1L);
            msg.setContent("Hello");
            when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(msg));
            when(userMapper.selectById(1L)).thenReturn(alice);

            List<MessageVO> result = messageService.groupHistory(1L, 100L, 1, 20, null);

            assertThat(result).hasSize(1);
        }
    }
}
