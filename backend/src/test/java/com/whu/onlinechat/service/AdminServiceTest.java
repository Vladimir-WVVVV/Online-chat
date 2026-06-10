package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.AdminLog;
import com.whu.onlinechat.entity.Message;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.AdminLogMapper;
import com.whu.onlinechat.mapper.MessageMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.AdminMetricsVO;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 单元测试")
class AdminServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private MessageMapper messageMapper;
    @Mock private AdminLogMapper adminLogMapper;
    @Mock private OnlineUserService onlineUserService;
    @InjectMocks private AdminService adminService;

    private User bob;

    @BeforeEach
    void setUp() {
        bob = new User();
        bob.setId(2L);
        bob.setUsername("bob");
        bob.setNickname("Bob");
        bob.setRole("USER");
        bob.setStatus("ONLINE");
    }

    @Nested
    @DisplayName("封禁用户")
    class Ban {

        @Test
        @DisplayName("正常封禁普通用户")
        void shouldBanUser() {
            when(userMapper.selectById(2L)).thenReturn(bob);

            adminService.ban(1L, 2L);

            assertThat(bob.getStatus()).isEqualTo("BANNED");
            verify(userMapper).updateById(any(User.class));
            verify(adminLogMapper).insert(any(AdminLog.class));
        }

        @Test
        @DisplayName("不能封禁管理员")
        void shouldThrowWhenBanningAdmin() {
            bob.setRole("ADMIN");
            when(userMapper.selectById(2L)).thenReturn(bob);

            assertThatThrownBy(() -> adminService.ban(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessage("不能封禁管理员");
        }

        @Test
        @DisplayName("封禁不存在的用户")
        void shouldThrowWhenUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> adminService.ban(1L, 999L))
                .isInstanceOf(BizException.class)
                .hasMessage("用户不存在");
        }
    }

    @Nested
    @DisplayName("解封用户")
    class Unban {

        @Test
        @DisplayName("正常解封用户")
        void shouldUnbanUser() {
            bob.setStatus("BANNED");
            when(userMapper.selectById(2L)).thenReturn(bob);

            adminService.unban(1L, 2L);

            assertThat(bob.getStatus()).isEqualTo("OFFLINE");
            verify(adminLogMapper).insert(any(AdminLog.class));
        }
    }

    @Nested
    @DisplayName("指标统计")
    class Metrics {

        @Test
        @DisplayName("返回今日指标数据")
        void shouldReturnMetrics() {
            when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
            when(onlineUserService.onlineCount()).thenReturn(5L);

            AdminMetricsVO result = adminService.metrics();

            assertThat(result.todayMessageCount()).isEqualTo(100L);
            assertThat(result.todayNewUserCount()).isEqualTo(10L);
            assertThat(result.onlineCount()).isEqualTo(5L);
        }
    }
}
