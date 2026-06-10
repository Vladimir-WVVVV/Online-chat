package com.whu.onlinechat.config;

import com.whu.onlinechat.security.JwtAuthenticationFilter;
import com.whu.onlinechat.security.JwtProperties;
import com.whu.onlinechat.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityConfig 单元测试")
class SecurityConfigTest {

    @Test
    @DisplayName("passwordEncoder 使用 BCrypt")
    void shouldUseBCryptPasswordEncoder() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("BCrypt 编码后可匹配")
    void shouldEncodeAndMatch() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        String encoded = encoder.encode("123456");
        assertThat(encoder.matches("123456", encoded)).isTrue();
        assertThat(encoder.matches("wrong", encoded)).isFalse();
    }

    @Test
    @DisplayName("CORS 配置源可用")
    void shouldProvideCorsConfigurationSource() {
        SecurityConfig config = new SecurityConfig();
        CorsConfigurationSource source = config.corsConfigurationSource();
        assertThat(source).isNotNull();

        var config2 = source.getCorsConfiguration(new MockHttpServletRequest());
        assertThat(config2).isNotNull();
    }

    @Test
    @DisplayName("JwtProperties 正确绑定配置")
    void shouldBindJwtProperties() {
        JwtProperties props = new JwtProperties("test-secret", 120);
        assertThat(props.secret()).isEqualTo("test-secret");
        assertThat(props.expireMinutes()).isEqualTo(120);
    }
}
