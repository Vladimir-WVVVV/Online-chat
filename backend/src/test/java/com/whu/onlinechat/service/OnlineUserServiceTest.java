package com.whu.onlinechat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnlineUserService 单元测试")
class OnlineUserServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private SetOperations<String, String> setOps;
    @InjectMocks private OnlineUserService onlineUserService;

    @Nested
    @DisplayName("上线")
    class Online {

        @Test
        @DisplayName("用户上线后状态为在线")
        void shouldSetUserOnline() {
            when(redisTemplate.opsForSet()).thenReturn(setOps);

            onlineUserService.online(1L, "session-1");

            assertThat(onlineUserService.isOnline(1L)).isTrue();
        }

        @Test
        @DisplayName("Redis 异常时不影响内存状态")
        void shouldStillWorkWhenRedisFails() {
            when(redisTemplate.opsForSet()).thenThrow(new RuntimeException("redis down"));

            onlineUserService.online(1L, "session-1");

            // Redis 只是 best-effort，内存状态仍然更新
            assertThat(onlineUserService.isOnline(1L)).isTrue();
        }
    }

    @Nested
    @DisplayName("下线")
    class Offline {

        @Test
        @DisplayName("用户下线后状态为离线")
        void shouldSetUserOffline() {
            when(redisTemplate.opsForSet()).thenReturn(setOps);
            onlineUserService.online(1L, "session-1");

            onlineUserService.offline(1L, "session-1");

            assertThat(onlineUserService.isOnline(1L)).isFalse();
        }

        @Test
        @DisplayName("多会话时仅移除一个会话")
        void shouldKeepOnlineWhenOtherSessionRemains() {
            when(redisTemplate.opsForSet()).thenReturn(setOps);
            onlineUserService.online(1L, "session-1");
            onlineUserService.online(1L, "session-2");

            onlineUserService.offline(1L, "session-1");

            // 还有一个 session 在线
            assertThat(onlineUserService.isOnline(1L)).isTrue();
        }
    }

    @Nested
    @DisplayName("在线人数统计")
    class OnlineCount {

        @Test
        @DisplayName("返回当前在线人数")
        void shouldReturnOnlineCount() {
            when(redisTemplate.opsForSet()).thenReturn(setOps);
            onlineUserService.online(1L, "s1");
            onlineUserService.online(2L, "s2");

            assertThat(onlineUserService.onlineCount()).isEqualTo(2L);
        }
    }
}
