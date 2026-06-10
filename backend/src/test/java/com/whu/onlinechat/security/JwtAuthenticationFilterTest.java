package com.whu.onlinechat.security;

import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 单元测试")
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @InjectMocks private JwtAuthenticationFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("正常认证")
    class Authenticated {

        @Test
        @DisplayName("有效 Token 设置认证信息")
        void shouldSetAuthenticationForValidToken() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
            CurrentUser user = new CurrentUser(1L, "alice", "USER");
            when(jwtService.parse("valid.token.here")).thenReturn(user);
            User entity = new User();
            entity.setId(1L);
            entity.setStatus("ONLINE");
            when(userMapper.selectById(1L)).thenReturn(entity);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("管理员 Token 设置 ROLE_ADMIN")
        void shouldSetAdminRole() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer admin.token");
            CurrentUser admin = new CurrentUser(0L, "admin", "ADMIN");
            when(jwtService.parse("admin.token")).thenReturn(admin);
            User entity = new User();
            entity.setId(0L);
            entity.setRole("ADMIN");
            entity.setStatus("ONLINE");
            when(userMapper.selectById(0L)).thenReturn(entity);

            filter.doFilterInternal(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))).isTrue();
        }
    }

    @Nested
    @DisplayName("拒绝认证")
    class Unauthenticated {

        @Test
        @DisplayName("无 Authorization 头时放行但不认证")
        void shouldPassWithoutAuthHeader() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("非 Bearer 格式的 Token 不认证")
        void shouldPassOnNonBearerToken() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Basic YWxpY2U6MTIzNDU2");

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("被封禁用户的 Token 不认证")
        void shouldRejectBannedUser() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
            CurrentUser user = new CurrentUser(1L, "alice", "USER");
            when(jwtService.parse("valid.token")).thenReturn(user);
            User entity = new User();
            entity.setId(1L);
            entity.setStatus("BANNED");
            when(userMapper.selectById(1L)).thenReturn(entity);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("不存在的用户的 Token 不认证")
        void shouldRejectNonexistentUser() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
            CurrentUser user = new CurrentUser(999L, "ghost", "USER");
            when(jwtService.parse("valid.token")).thenReturn(user);
            when(userMapper.selectById(999L)).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("解析异常的 Token 不认证")
        void shouldClearContextOnParseException() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
            when(jwtService.parse("invalid.token")).thenThrow(new RuntimeException("invalid"));

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
