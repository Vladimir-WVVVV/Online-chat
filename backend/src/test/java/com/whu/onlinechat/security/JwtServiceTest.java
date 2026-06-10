package com.whu.onlinechat.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService 单元测试")
class JwtServiceTest {

    private static final String SECRET = "this-is-a-test-secret-key-with-32-plus-chars!!";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, 60);
        jwtService = new JwtService(properties);
    }

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("生成包含用户信息的 JWT")
        void shouldGenerateTokenWithUserInfo() {
            CurrentUser user = new CurrentUser(1L, "alice", "USER");

            String token = jwtService.generate(user);

            assertThat(token).isNotBlank();
            // 验证是三段式 JWT
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("解析 JWT 获取用户信息")
        void shouldParseToken() {
            CurrentUser original = new CurrentUser(2L, "bob", "USER");
            String token = jwtService.generate(original);

            CurrentUser parsed = jwtService.parse(token);

            assertThat(parsed.id()).isEqualTo(2L);
            assertThat(parsed.username()).isEqualTo("bob");
            assertThat(parsed.role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("解析包含管理员角色的 Token")
        void shouldParseAdminToken() {
            CurrentUser admin = new CurrentUser(0L, "admin", "ADMIN");
            String token = jwtService.generate(admin);

            CurrentUser parsed = jwtService.parse(token);

            assertThat(parsed.role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("过期 Token 解析失败")
        void shouldThrowWhenTokenExpired() {
            // 使用 0 分钟过期时间生成立即过期的 Token
            JwtProperties expiredProp = new JwtProperties(SECRET, 0);
            JwtService expiredService = new JwtService(expiredProp);
            String token = expiredService.generate(new CurrentUser(1L, "alice", "USER"));

            // 等待 token 过期（0 分钟意味着立即过期）
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            assertThatThrownBy(() -> expiredService.parse(token))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("被篡改的 Token 解析失败")
        void shouldThrowWhenTokenTampered() {
            String token = jwtService.generate(new CurrentUser(1L, "alice", "USER"));
            String tampered = token + "tampered";

            assertThatThrownBy(() -> jwtService.parse(tampered))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("无效签名的 Token 解析失败")
        void shouldThrowWhenInvalidSignature() {
            JwtProperties otherProp = new JwtProperties("another-secret-key-at-least-32-chars-long!!!!", 60);
            JwtService otherService = new JwtService(otherProp);
            String token = otherService.generate(new CurrentUser(1L, "alice", "USER"));

            assertThatThrownBy(() -> jwtService.parse(token))
                .isInstanceOf(Exception.class);
        }
    }
}
