package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.ChangePasswordRequest;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.UserService;
import com.whu.onlinechat.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 单元测试")
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController userController;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("获取个人信息")
    class Me {

        @Test
        @DisplayName("正常返回个人信息")
        void shouldReturnProfile() {
            UserVO vo = new UserVO(1L, "alice", "alice@test.com", "Alice", null, null, "ONLINE", "USER");
            when(userService.getProfile(1L)).thenReturn(vo);

            ApiResult<UserVO> result = userController.me(alice);

            assertThat(result.data().username()).isEqualTo("alice");
        }
    }

    @Nested
    @DisplayName("更新个人信息")
    class UpdateMe {

        @Test
        @DisplayName("正常更新个人信息")
        void shouldUpdateProfile() {
            UpdateProfileRequest req = new UpdateProfileRequest("NewAlice", null, null);
            UserVO vo = new UserVO(1L, "alice", "alice@test.com", "NewAlice", null, null, "ONLINE", "USER");
            when(userService.updateProfile(eq(1L), any())).thenReturn(vo);

            ApiResult<UserVO> result = userController.updateMe(alice, req);

            assertThat(result.data().nickname()).isEqualTo("NewAlice");
        }
    }

    @Nested
    @DisplayName("修改密码")
    class ChangePwd {

        @Test
        @DisplayName("正常修改密码")
        void shouldChangePassword() {
            ChangePasswordRequest req = new ChangePasswordRequest("old", "new123");

            ApiResult<Void> result = userController.changePassword(alice, req);

            assertThat(result.code()).isEqualTo(0);
            verify(userService).changePassword(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("搜索用户")
    class Search {

        @Test
        @DisplayName("正常搜索用户")
        void shouldSearch() {
            UserVO bob = new UserVO(2L, "bob", "bob@test.com", "Bob", null, null, "ONLINE", "USER");
            when(userService.search(1L, "bob")).thenReturn(List.of(bob));

            ApiResult<List<UserVO>> result = userController.search(alice, "bob");

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).username()).isEqualTo("bob");
        }

        @Test
        @DisplayName("空关键词也能搜索")
        void shouldSearchWithNullKeyword() {
            when(userService.search(1L, null)).thenReturn(List.of());

            ApiResult<List<UserVO>> result = userController.search(alice, null);

            assertThat(result.data()).isEmpty();
        }
    }
}
