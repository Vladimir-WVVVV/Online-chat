package com.whu.onlinechat.controller;

import com.whu.onlinechat.service.AdminService;
import com.whu.onlinechat.vo.AdminMetricsVO;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminController 集成测试")
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AdminService adminService;

    @Nested
    @DisplayName("管理员操作")
    class AdminAccess {

        @Test
        @DisplayName("管理员查看用户列表")
        @WithMockUser(roles = "ADMIN")
        void shouldListUsers() throws Exception {
            when(adminService.users()).thenReturn(List.of(
                new UserVO(1L, "alice", "alice@test.com", "Alice", null, null, "ONLINE", "USER")));

            mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("alice"));
        }

        @Test
        @DisplayName("管理员获取指标")
        @WithMockUser(roles = "ADMIN")
        void shouldGetMetrics() throws Exception {
            when(adminService.metrics()).thenReturn(new AdminMetricsVO(5L, 100L, 10L));

            mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onlineCount").value(5L));
        }

        @Test
        @DisplayName("普通用户访问管理员接口被拒绝")
        @WithMockUser(roles = "USER")
        void shouldRejectNormalUser() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("未认证用户访问管理员接口被拒绝")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is4xxClientError());
        }
    }
}
