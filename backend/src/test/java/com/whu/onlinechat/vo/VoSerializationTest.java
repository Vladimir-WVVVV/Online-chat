package com.whu.onlinechat.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whu.onlinechat.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VO 序列化/转换测试")
class VoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("UserVO")
    class UserVOTests {

        @Test
        @DisplayName("从 User 实体正确转换")
        void shouldConvertFromEntity() {
            User user = new User();
            user.setId(1L);
            user.setUsername("alice");
            user.setEmail("alice@test.com");
            user.setNickname("Alice");
            user.setAvatarUrl("http://avatar.png");
            user.setBio("Hello");
            user.setStatus("ONLINE");
            user.setRole("USER");

            UserVO vo = UserVO.from(user);

            assertThat(vo.id()).isEqualTo(1L);
            assertThat(vo.username()).isEqualTo("alice");
            assertThat(vo.email()).isEqualTo("alice@test.com");
            assertThat(vo.nickname()).isEqualTo("Alice");
            assertThat(vo.avatarUrl()).isEqualTo("http://avatar.png");
            assertThat(vo.bio()).isEqualTo("Hello");
            assertThat(vo.status()).isEqualTo("ONLINE");
            assertThat(vo.role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("JSON 序列化正确")
        void shouldSerializeToJson() throws Exception {
            UserVO vo = new UserVO(1L, "alice", "alice@test.com", "Alice", null, null, "ONLINE", "USER");

            String json = objectMapper.writeValueAsString(vo);

            assertThat(json).contains("\"id\":1");
            assertThat(json).contains("\"username\":\"alice\"");
            assertThat(json).contains("\"status\":\"ONLINE\"");
        }
    }

    @Nested
    @DisplayName("LoginVO")
    class LoginVOTests {

        @Test
        @DisplayName("JSON 序列化包含 token 和 user")
        void shouldSerializeTokenAndUser() throws Exception {
            UserVO userVo = new UserVO(1L, "alice", "alice@test.com", "Alice", null, null, "ONLINE", "USER");
            LoginVO loginVo = new LoginVO("jwt.token", userVo);

            String json = objectMapper.writeValueAsString(loginVo);

            assertThat(json).contains("\"token\":\"jwt.token\"");
            assertThat(json).contains("\"user\":");
            assertThat(json).contains("\"username\":\"alice\"");
        }
    }

    @Nested
    @DisplayName("AdminMetricsVO")
    class AdminMetricsVOTests {

        @Test
        @DisplayName("保存正确的统计数据")
        void shouldHoldMetrics() {
            AdminMetricsVO vo = new AdminMetricsVO(5L, 100L, 10L);

            assertThat(vo.onlineCount()).isEqualTo(5L);
            assertThat(vo.todayMessageCount()).isEqualTo(100L);
            assertThat(vo.todayNewUserCount()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("NotificationVO")
    class NotificationVOTests {

        @Test
        @DisplayName("read 字段正确映射")
        void shouldMapReadFlag() {
            var unread = new NotificationVO(1L, "FRIEND_REQUEST", "content", false, null);
            var read = new NotificationVO(2L, "FRIEND_REQUEST", "content", true, null);

            assertThat(unread.read()).isFalse();
            assertThat(read.read()).isTrue();
        }
    }
}
