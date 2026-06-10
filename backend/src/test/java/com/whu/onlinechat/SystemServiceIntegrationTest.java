package com.whu.onlinechat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whu.onlinechat.ai.AiChatRequest;
import com.whu.onlinechat.ai.AiChatResponse;
import com.whu.onlinechat.ai.AiService;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.FriendRequestCreateRequest;
import com.whu.onlinechat.dto.LoginRequest;
import com.whu.onlinechat.dto.RegisterRequest;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.service.AuthService;
import com.whu.onlinechat.service.FriendService;
import com.whu.onlinechat.service.MessageService;
import com.whu.onlinechat.service.UserService;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SystemServiceIntegrationTest {
    @Autowired private AuthService authService;
    @Autowired private UserService userService;
    @Autowired private FriendService friendService;
    @Autowired private AiService aiService;
    @Autowired private MessageService messageService;

    @Test
    void coreUserFriendAndAiFlowPersistsAndLoadsForBothPrivateParticipants() {
        UserVO alice = authService.register(new RegisterRequest("test_alice", "test_alice@example.com", "secret1"));
        UserVO bob = authService.register(new RegisterRequest("test_bob", "test_bob@example.com", "secret2"));

        assertThatThrownBy(() -> authService.register(
            new RegisterRequest("test_alice", "another@example.com", "secret3")))
            .isInstanceOf(BizException.class)
            .hasMessage("用户名已存在");
        assertThat(authService.login(new LoginRequest("test_alice", "secret1")).user().id()).isEqualTo(alice.id());
        assertThatThrownBy(() -> authService.login(new LoginRequest("test_alice", "wrong")))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("用户名或密码错误");

        UserVO updated = userService.updateProfile(alice.id(),
            new UpdateProfileRequest("重复昵称", "/api/users/avatars/demo.png", "新的简介"));
        assertThat(updated.username()).isEqualTo("test_alice");
        assertThat(userService.getProfile(alice.id()).nickname()).isEqualTo("重复昵称");
        assertThat(userService.getProfile(alice.id()).bio()).isEqualTo("新的简介");

        assertThat(userService.search(alice.id(), "test_bob"))
            .extracting(UserVO::id)
            .containsExactly(bob.id());
        friendService.request(alice.id(), new FriendRequestCreateRequest(bob.id(), "测试申请"));
        Long requestId = friendService.received(bob.id()).get(0).id();
        friendService.accept(bob.id(), requestId);
        assertThat(friendService.isFriend(alice.id(), bob.id())).isTrue();
        assertThat(friendService.isFriend(bob.id(), alice.id())).isTrue();

        AiChatResponse ai = aiService.chat(alice.id(), new AiChatRequest("PRIVATE", bob.id(), "QA", "解释 WebSocket"));
        assertThat(ai.message().messageId()).isNotNull();
        assertThat(messageService.privateHistory(alice.id(), bob.id(), 1, 20, null))
            .extracting(message -> message.messageId())
            .contains(ai.message().messageId());
        assertThat(messageService.privateHistory(bob.id(), alice.id(), 1, 20, null))
            .extracting(message -> message.messageId())
            .contains(ai.message().messageId());
    }
}
