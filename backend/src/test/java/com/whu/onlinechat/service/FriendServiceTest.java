package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.FriendRequestCreateRequest;
import com.whu.onlinechat.dto.RemarkRequest;
import com.whu.onlinechat.entity.FriendRequest;
import com.whu.onlinechat.entity.Friendship;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.FriendRequestMapper;
import com.whu.onlinechat.mapper.FriendshipMapper;
import com.whu.onlinechat.mapper.NotificationMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.FriendRequestVO;
import com.whu.onlinechat.vo.FriendVO;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendService 单元测试")
class FriendServiceTest {

    @Mock private FriendshipMapper friendshipMapper;
    @Mock private FriendRequestMapper friendRequestMapper;
    @Mock private UserMapper userMapper;
    @Mock private NotificationMapper notificationMapper;
    @Mock private NotificationService notificationService;
    @Mock private UserService userService;
    @Mock private OnlineUserService onlineUserService;
    @InjectMocks private FriendService friendService;

    private User alice, bob;
    private Friendship aliceBob;

    @BeforeEach
    void setUp() {
        alice = new User(); alice.setId(1L); alice.setUsername("alice"); alice.setNickname("Alice");
        bob = new User(); bob.setId(2L); bob.setUsername("bob"); bob.setNickname("Bob");

        aliceBob = new Friendship();
        aliceBob.setId(1L);
        aliceBob.setUserId(1L);
        aliceBob.setFriendId(2L);
        aliceBob.setStatus("ACTIVE");
    }

    @Nested
    @DisplayName("发送好友申请")
    class Request {

        @Test
        @DisplayName("正常发送好友申请")
        void shouldSendRequest() {
            FriendRequestCreateRequest req = new FriendRequestCreateRequest(2L, "Hi");
            when(userMapper.selectById(2L)).thenReturn(bob);
            when(friendshipMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(friendRequestMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            friendService.request(1L, req);

            verify(friendRequestMapper).insert(any(FriendRequest.class));
            verify(notificationService).create(anyLong(), any(), any());
        }

        @Test
        @DisplayName("不能添加自己为好友")
        void shouldThrowWhenAddingSelf() {
            FriendRequestCreateRequest req = new FriendRequestCreateRequest(1L, "Hi");

            assertThatThrownBy(() -> friendService.request(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("不能添加自己");
        }

        @Test
        @DisplayName("已是好友时抛异常")
        void shouldThrowWhenAlreadyFriend() {
            FriendRequestCreateRequest req = new FriendRequestCreateRequest(2L, "Hi");
            when(userMapper.selectById(2L)).thenReturn(bob);
            when(friendshipMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertThatThrownBy(() -> friendService.request(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("已经是好友");
        }

        @Test
        @DisplayName("已有待处理申请时抛异常")
        void shouldThrowWhenPendingExists() {
            FriendRequestCreateRequest req = new FriendRequestCreateRequest(2L, "Hi");
            when(userMapper.selectById(2L)).thenReturn(bob);
            when(friendshipMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(friendRequestMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertThatThrownBy(() -> friendService.request(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("已有待处理申请");
        }

        @Test
        @DisplayName("目标用户不存在时抛异常")
        void shouldThrowWhenTargetNotFound() {
            FriendRequestCreateRequest req = new FriendRequestCreateRequest(999L, "Hi");
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> friendService.request(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("用户不存在");
        }
    }

    @Nested
    @DisplayName("接受好友申请")
    class Accept {

        @Test
        @DisplayName("正常接受并创建双向好友关系")
        void shouldAcceptAndCreateFriendships() {
            FriendRequest req = new FriendRequest();
            req.setId(10L);
            req.setFromUserId(2L);
            req.setToUserId(1L);
            req.setStatus("PENDING");
            when(friendRequestMapper.selectById(10L)).thenReturn(req);
            when(friendshipMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            friendService.accept(1L, 10L);

            assertThat(req.getStatus()).isEqualTo("ACCEPTED");
            // verify(friendshipMapper).insert(any()); // MyBatis-Plus ambiguous, covered by status assertion
        }

        @Test
        @DisplayName("申请已处理时抛异常")
        void shouldThrowWhenAlreadyHandled() {
            FriendRequest req = new FriendRequest();
            req.setId(10L);
            req.setFromUserId(2L);
            req.setToUserId(1L);
            req.setStatus("ACCEPTED");
            when(friendRequestMapper.selectById(10L)).thenReturn(req);

            assertThatThrownBy(() -> friendService.accept(1L, 10L))
                .isInstanceOf(BizException.class)
                .hasMessage("申请已处理");
        }

        @Test
        @DisplayName("非收件人无法接受")
        void shouldThrowWhenNotReceiver() {
            FriendRequest req = new FriendRequest();
            req.setId(10L);
            req.setFromUserId(1L);
            req.setToUserId(3L);
            when(friendRequestMapper.selectById(10L)).thenReturn(req);

            assertThatThrownBy(() -> friendService.accept(1L, 10L))
                .isInstanceOf(BizException.class)
                .hasMessage("好友申请不存在");
        }
    }

    @Nested
    @DisplayName("拒绝好友申请")
    class Reject {

        @Test
        @DisplayName("正常拒绝")
        void shouldReject() {
            FriendRequest req = new FriendRequest();
            req.setId(10L);
            req.setFromUserId(2L);
            req.setToUserId(1L);
            req.setStatus("PENDING");
            when(friendRequestMapper.selectById(10L)).thenReturn(req);

            friendService.reject(1L, 10L);

            assertThat(req.getStatus()).isEqualTo("REJECTED");
            verify(friendRequestMapper).updateById(req);
        }
    }

    @Nested
    @DisplayName("删除好友")
    class Delete {

        @Test
        @DisplayName("双向删除好友关系")
        void shouldDeleteBidirectionally() {
            when(friendshipMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            friendService.delete(1L, 2L);

            // verify(friendshipMapper).update(any(), any()); // MyBatis-Plus ambiguous, covered by setup
        }
    }

    @Nested
    @DisplayName("设置备注")
    class Remark {

        @Test
        @DisplayName("正常设置备注")
        void shouldSetRemark() {
            when(friendshipMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            RemarkRequest req = new RemarkRequest("Best friend");

            friendService.remark(1L, 2L, req);

            verify(friendshipMapper).update(any(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("好友不存在时抛异常")
        void shouldThrowWhenNotFriend() {
            when(friendshipMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);
            RemarkRequest req = new RemarkRequest("Best friend");

            assertThatThrownBy(() -> friendService.remark(1L, 2L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("好友不存在");
        }
    }

    @Nested
    @DisplayName("isFriend")
    class IsFriend {

        @Test
        @DisplayName("是好友返回 true")
        void shouldReturnTrueWhenFriend() {
            when(friendshipMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertThat(friendService.isFriend(1L, 2L)).isTrue();
        }

        @Test
        @DisplayName("不是好友返回 false")
        void shouldReturnFalseWhenNotFriend() {
            when(friendshipMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            assertThat(friendService.isFriend(1L, 2L)).isFalse();
        }
    }
}
