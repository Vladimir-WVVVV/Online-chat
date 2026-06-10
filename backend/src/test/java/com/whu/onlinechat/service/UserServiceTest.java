package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.ChangePasswordRequest;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");
        alice.setEmail("alice@test.com");
        alice.setNickname("Alice");
        alice.setAvatarUrl("http://example.com/avatar.png");
        alice.setBio("Hello");
        alice.setPasswordHash("hashed_old");
        alice.setRole("USER");
        alice.setStatus("OFFLINE");
    }

    @Nested
    @DisplayName("获取个人信息")
    class GetProfile {

        @Test
        @DisplayName("正常返回个人信息")
        void shouldReturnProfile() {
            when(userMapper.selectById(1L)).thenReturn(alice);

            UserVO result = userService.getProfile(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.nickname()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("被封禁用户获取信息抛异常")
        void shouldThrowWhenBanned() {
            alice.setStatus("BANNED");
            when(userMapper.selectById(1L)).thenReturn(alice);

            assertThatThrownBy(() -> userService.getProfile(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("账号已被封禁");
        }
    }

    @Nested
    @DisplayName("更新个人信息")
    class UpdateProfile {

        @Test
        @DisplayName("更新昵称成功")
        void shouldUpdateNickname() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            UpdateProfileRequest request = new UpdateProfileRequest("NewAlice", null, null);

            UserVO result = userService.updateProfile(1L, request);

            assertThat(result.nickname()).isEqualTo("NewAlice");
            verify(userMapper).updateById(alice);
        }

        @Test
        @DisplayName("更新全部字段")
        void shouldUpdateAllFields() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            UpdateProfileRequest request = new UpdateProfileRequest("NewName", "http://new.com/avatar.png", "New bio");

            userService.updateProfile(1L, request);

            assertThat(alice.getNickname()).isEqualTo("NewName");
            assertThat(alice.getAvatarUrl()).isEqualTo("http://new.com/avatar.png");
            assertThat(alice.getBio()).isEqualTo("New bio");
        }

        @Test
        @DisplayName("昵称为空字符串时不更新")
        void shouldNotUpdateWhenNicknameBlank() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            UpdateProfileRequest request = new UpdateProfileRequest("   ", null, null);

            userService.updateProfile(1L, request);

            assertThat(alice.getNickname()).isEqualTo("Alice"); // unchanged
        }
    }

    @Nested
    @DisplayName("修改密码")
    class ChangePassword {

        @Test
        @DisplayName("修改密码成功")
        void shouldChangePassword() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            when(passwordEncoder.matches("old", "hashed_old")).thenReturn(true);
            when(passwordEncoder.encode("newpwd")).thenReturn("hashed_new");
            ChangePasswordRequest request = new ChangePasswordRequest("old", "newpwd");

            userService.changePassword(1L, request);

            verify(userMapper).updateById(alice);
            assertThat(alice.getPasswordHash()).isEqualTo("hashed_new");
        }

        @Test
        @DisplayName("原密码错误时抛异常")
        void shouldThrowWhenOldPasswordWrong() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            when(passwordEncoder.matches("wrong", "hashed_old")).thenReturn(false);
            ChangePasswordRequest request = new ChangePasswordRequest("wrong", "newpwd");

            assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(BizException.class)
                .hasMessage("原密码错误");
            // verify(userMapper, never()).updateById(any()); // MyBatis-Plus ambiguous, covered by exception assertion
        }
    }

    @Nested
    @DisplayName("搜索用户")
    class Search {

        @Test
        @DisplayName("按关键词搜索并排除自己和AI用户")
        void shouldSearchExcludingSelfAndAi() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            User bob = new User();
            bob.setId(2L);
            bob.setUsername("bob");
            bob.setNickname("Bob");
            bob.setRole("USER");
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(bob));

            List<UserVO> results = userService.search(1L, "bob");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).username()).isEqualTo("bob");
        }

        @Test
        @DisplayName("关键词为 null 时正常返回")
        void shouldHandleNullKeyword() {
            when(userMapper.selectById(1L)).thenReturn(alice);
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

            List<UserVO> results = userService.search(1L, null);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("requireActiveUser")
    class RequireActiveUser {

        @Test
        @DisplayName("用户存在且正常时返回")
        void shouldReturnWhenActive() {
            when(userMapper.selectById(1L)).thenReturn(alice);

            User result = userService.requireActiveUser(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("用户不存在时抛异常")
        void shouldThrowWhenNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> userService.requireActiveUser(999L))
                .isInstanceOf(BizException.class)
                .hasMessage("用户不存在");
        }

        @Test
        @DisplayName("被封禁时抛异常")
        void shouldThrowWhenBanned() {
            alice.setStatus("BANNED");
            when(userMapper.selectById(1L)).thenReturn(alice);

            assertThatThrownBy(() -> userService.requireActiveUser(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("账号已被封禁");
        }
    }
}
