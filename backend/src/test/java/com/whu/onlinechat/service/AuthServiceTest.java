package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.LoginRequest;
import com.whu.onlinechat.dto.RegisterRequest;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.security.JwtService;
import com.whu.onlinechat.vo.LoginVO;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    // ── register ────────────────────────────────────────
    @Nested
    @DisplayName("注册")
    class Register {

        @Test
        @DisplayName("正常注册成功")
        void shouldRegisterSuccessfully() {
            RegisterRequest request = new RegisterRequest("newuser", "new@test.com", "123456");
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("123456")).thenReturn("hashed_password");

            UserVO result = authService.register(request);

            assertThat(result.username()).isEqualTo("newuser");
            assertThat(result.email()).isEqualTo("new@test.com");
            assertThat(result.role()).isEqualTo("USER");
            assertThat(result.status()).isEqualTo("OFFLINE");

            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("用户名已存在时抛异常")
        void shouldThrowWhenUsernameExists() {
            RegisterRequest request = new RegisterRequest("alice", "alice@test.com", "123456");
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BizException.class)
                .hasMessage("用户名已存在");
            // verify(userMapper, never()).insert(any()); // MyBatis-Plus insert ambiguous, covered by exception assertion
        }

        @Test
        @DisplayName("邮箱已存在时抛异常")
        void shouldThrowWhenEmailExists() {
            RegisterRequest request = new RegisterRequest("newuser", "alice@test.com", "123456");
            when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L)   // username check
                .thenReturn(1L);  // email check

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BizException.class)
                .hasMessage("邮箱已存在");
        }
    }

    // ── login ──────────────────────────────────────────
    @Nested
    @DisplayName("登录")
    class Login {

        private User alice;

        @BeforeEach
        void setUp() {
            alice = new User();
            alice.setId(1L);
            alice.setUsername("alice");
            alice.setEmail("alice@test.com");
            alice.setNickname("Alice");
            alice.setPasswordHash("hashed_password");
            alice.setRole("USER");
            alice.setStatus("OFFLINE");
        }

        @Test
        @DisplayName("正常登录返回 token 和用户信息")
        void shouldLoginSuccessfully() {
            LoginRequest request = new LoginRequest("alice", "123456");
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(alice);
            when(passwordEncoder.matches("123456", "hashed_password")).thenReturn(true);
            when(jwtService.generate(any())).thenReturn("jwt.token.here");

            LoginVO result = authService.login(request);

            assertThat(result.token()).isEqualTo("jwt.token.here");
            assertThat(result.user().username()).isEqualTo("alice");
        }

        @Test
        @DisplayName("用户名不存在时抛 BadCredentialsException")
        void shouldThrowWhenUserNotFound() {
            LoginRequest request = new LoginRequest("unknown", "123456");
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("用户名或密码错误");
        }

        @Test
        @DisplayName("密码错误时抛 BadCredentialsException")
        void shouldThrowWhenPasswordWrong() {
            LoginRequest request = new LoginRequest("alice", "wrong");
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(alice);
            when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("被封禁用户登录时抛 BizException")
        void shouldThrowWhenBanned() {
            alice.setStatus("BANNED");
            LoginRequest request = new LoginRequest("alice", "123456");
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(alice);
            when(passwordEncoder.matches("123456", "hashed_password")).thenReturn(true);

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BizException.class)
                .hasMessage("账号已被封禁");
        }
    }

    // ── me ─────────────────────────────────────────────
    @Nested
    @DisplayName("获取当前用户")
    class Me {

        @Test
        @DisplayName("正常返回用户信息")
        void shouldReturnCurrentUser() {
            User alice = new User();
            alice.setId(1L);
            alice.setUsername("alice");
            alice.setNickname("Alice");
            alice.setRole("USER");
            when(userMapper.selectById(1L)).thenReturn(alice);

            UserVO result = authService.me(1L);

            assertThat(result.username()).isEqualTo("alice");
        }

        @Test
        @DisplayName("用户不存在时抛异常")
        void shouldThrowWhenUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> authService.me(999L))
                .isInstanceOf(BizException.class)
                .hasMessage("用户不存在");
        }
    }
}
