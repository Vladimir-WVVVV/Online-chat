package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.FriendRequestCreateRequest;
import com.whu.onlinechat.dto.RemarkRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.FriendService;
import com.whu.onlinechat.vo.FriendRequestVO;
import com.whu.onlinechat.vo.FriendVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendController 单元测试")
class FriendControllerTest {

    @Mock private FriendService friendService;
    @InjectMocks private FriendController friendController;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("好友列表")
    class FriendList {

        @Test
        @DisplayName("正常返回好友列表")
        void shouldReturnFriendList() {
            FriendVO friend = new FriendVO(2L, "bob", "Bob", null, null, "ONLINE", "好友", 0L);
            when(friendService.list(1L)).thenReturn(java.util.List.of(friend));

            var result = friendController.list(alice);

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).username()).isEqualTo("bob");
        }
    }

    @Nested
    @DisplayName("发送好友申请")
    class SendRequest {

        @Test
        @DisplayName("正常发送好友申请")
        void shouldSendRequest() {
            var req = new FriendRequestCreateRequest(2L, "Hi");

            var result = friendController.request(alice, req);

            assertThat(result.code()).isEqualTo(0);
            verify(friendService).request(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("收到的申请")
    class ReceivedRequests {

        @Test
        @DisplayName("返回收到的申请列表")
        void shouldReturnReceived() {
            var vo = new FriendRequestVO(10L, 2L, "bob", "Bob", 1L, "alice", "Alice",
                "PENDING", "Hi", LocalDateTime.now());
            when(friendService.received(1L)).thenReturn(java.util.List.of(vo));

            var result = friendController.received(alice);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).status()).isEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("发出的申请")
    class SentRequests {

        @Test
        @DisplayName("返回发出的申请列表")
        void shouldReturnSent() {
            when(friendService.sent(1L)).thenReturn(java.util.List.of());

            var result = friendController.sent(alice);

            assertThat(result.data()).isEmpty();
        }
    }

    @Nested
    @DisplayName("接受申请")
    class AcceptRequest {

        @Test
        @DisplayName("正常接受申请")
        void shouldAccept() {
            var result = friendController.accept(alice, 10L);

            assertThat(result.code()).isEqualTo(0);
            verify(friendService).accept(1L, 10L);
        }
    }

    @Nested
    @DisplayName("拒绝申请")
    class RejectRequest {

        @Test
        @DisplayName("正常拒绝申请")
        void shouldReject() {
            var result = friendController.reject(alice, 10L);

            assertThat(result.code()).isEqualTo(0);
            verify(friendService).reject(1L, 10L);
        }
    }

    @Nested
    @DisplayName("设置备注")
    class SetRemark {

        @Test
        @DisplayName("正常设置备注")
        void shouldRemark() {
            var req = new RemarkRequest("Best");

            var result = friendController.remark(alice, 2L, req);

            assertThat(result.code()).isEqualTo(0);
            verify(friendService).remark(eq(1L), eq(2L), any());
        }
    }

    @Nested
    @DisplayName("删除好友")
    class DeleteFriend {

        @Test
        @DisplayName("正常删除好友")
        void shouldDelete() {
            var result = friendController.delete(alice, 2L);

            assertThat(result.code()).isEqualTo(0);
            verify(friendService).delete(1L, 2L);
        }
    }
}
