package com.whu.onlinechat.controller;

import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.dto.CreateGroupRequest;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.service.GroupService;
import com.whu.onlinechat.vo.GroupMemberVO;
import com.whu.onlinechat.vo.GroupVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupController 单元测试")
class GroupControllerTest {

    @Mock private GroupService groupService;
    @InjectMocks private GroupController groupController;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("创建群聊")
    class Create {

        @Test
        @DisplayName("正常创建群聊")
        void shouldCreateGroup() {
            CreateGroupRequest req = new CreateGroupRequest("测试群", null, "desc", List.of(2L));
            GroupVO vo = new GroupVO(100L, "测试群", 1L, "Alice", null, "desc", 2L, 0L, LocalDateTime.now());
            when(groupService.create(eq(1L), any())).thenReturn(vo);

            ApiResult<GroupVO> result = groupController.create(alice, req);

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.data().name()).isEqualTo("测试群");
        }
    }

    @Nested
    @DisplayName("我的群聊")
    class Mine {

        @Test
        @DisplayName("返回群聊列表")
        void shouldReturnMyGroups() {
            when(groupService.mine(1L)).thenReturn(List.of());

            ApiResult<List<GroupVO>> result = groupController.mine(alice);

            assertThat(result.code()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("群聊详情")
    class Detail {

        @Test
        @DisplayName("返回群聊详情")
        void shouldReturnDetail() {
            GroupVO vo = new GroupVO(100L, "群", 1L, "Alice", null, "desc", 3L, 0L, LocalDateTime.now());
            when(groupService.detail(1L, 100L)).thenReturn(vo);

            ApiResult<GroupVO> result = groupController.detail(alice, 100L);

            assertThat(result.data().ownerId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("加入群聊")
    class Join {

        @Test
        @DisplayName("正常加入")
        void shouldJoin() {
            ApiResult<Void> result = groupController.join(alice, 100L);

            assertThat(result.code()).isEqualTo(0);
            verify(groupService).join(1L, 100L);
        }
    }

    @Nested
    @DisplayName("退出群聊")
    class Leave {

        @Test
        @DisplayName("正常退出")
        void shouldLeave() {
            ApiResult<Void> result = groupController.leave(alice, 100L);

            assertThat(result.code()).isEqualTo(0);
            verify(groupService).leave(1L, 100L);
        }
    }

    @Nested
    @DisplayName("群成员")
    class Members {

        @Test
        @DisplayName("返回群成员列表")
        void shouldReturnMembers() {
            GroupMemberVO member = new GroupMemberVO(1L, "alice", "Alice", null, "ONLINE", "OWNER", LocalDateTime.now());
            when(groupService.members(1L, 100L)).thenReturn(List.of(member));

            ApiResult<List<GroupMemberVO>> result = groupController.members(alice, 100L);

            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).role()).isEqualTo("OWNER");
        }
    }

    @Nested
    @DisplayName("移除成员")
    class Remove {

        @Test
        @DisplayName("正常移除成员")
        void shouldRemove() {
            ApiResult<Void> result = groupController.remove(alice, 100L, 2L);

            assertThat(result.code()).isEqualTo(0);
            verify(groupService).removeMember(1L, 100L, 2L);
        }
    }
}
