package com.whu.onlinechat.websocket;

import com.whu.onlinechat.service.OnlineUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketEventListener 单元测试")
class WebSocketEventListenerTest {

    @Mock private OnlineUserService onlineUserService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private WebSocketEventListener listener;

    @Test
    @DisplayName("UserPrincipal 类型和角色正确")
    void userPrincipalShouldImplementPrincipal() {
        var principal = new UserPrincipal(1L, "alice", "USER");

        assertThat(principal).isInstanceOf(java.security.Principal.class);
        assertThat(principal.getName()).isEqualTo("1");
        assertThat(principal.id()).isEqualTo(1L);
        assertThat(principal.username()).isEqualTo("alice");
        assertThat(principal.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("UserPrincipal 正确区分管理员和普通用户")
    void userPrincipalShouldDistinguishRoles() {
        var admin = new UserPrincipal(0L, "admin", "ADMIN");
        var user = new UserPrincipal(1L, "alice", "USER");

        assertThat(admin.role()).isEqualTo("ADMIN");
        assertThat(user.role()).isEqualTo("USER");
    }
}
