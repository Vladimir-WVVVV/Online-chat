package com.whu.onlinechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whu.onlinechat.dto.LoginRequest;
import com.whu.onlinechat.dto.RegisterRequest;
import com.whu.onlinechat.service.AuthService;
import com.whu.onlinechat.vo.LoginVO;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController 集成测试")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;

    @Nested
    @DisplayName("公开接口")
    class PublicEndpoints {

        @Test
        @DisplayName("正常注册返回用户信息")
        void shouldRegisterUser() throws Exception {
            RegisterRequest req = new RegisterRequest("newuser", "new@test.com", "123456");
            when(authService.register(any())).thenReturn(
                new UserVO(1L, "newuser", "new@test.com", "newuser", null, null, "OFFLINE", "USER"));

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("newuser"));
        }

        @Test
        @DisplayName("缺少必填字段时返回 400")
        void shouldReturn400OnInvalidInput() throws Exception {
            String invalidBody = "{\"username\":\"\",\"email\":\"bad\",\"password\":\"12\"}";
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody)
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("正常登录返回 token")
        void shouldLoginAndReturnToken() throws Exception {
            LoginRequest req = new LoginRequest("alice", "123456");
            when(authService.login(any())).thenReturn(
                new LoginVO("jwt.token.here",
                    new UserVO(1L, "alice", "alice@test.com", "Alice", null, null, "ONLINE", "USER")));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.user.username").value("alice"));
        }

        @Test
        @DisplayName("未认证用户访问受保护接口返回 4xx")
        void shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is4xxClientError());
        }
    }
}
