package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.CreateGroupRequest;
import com.whu.onlinechat.entity.ChatGroup;
import com.whu.onlinechat.entity.GroupMember;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.ChatGroupMapper;
import com.whu.onlinechat.mapper.GroupMemberMapper;
import com.whu.onlinechat.mapper.NotificationMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.GroupMemberVO;
import com.whu.onlinechat.vo.GroupVO;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService 单元测试")
class GroupServiceTest {

    @Mock private ChatGroupMapper groupMapper;
    @Mock private GroupMemberMapper memberMapper;
    @Mock private UserMapper userMapper;
    @Mock private NotificationMapper notificationMapper;
    @Mock private UserService userService;
    @Mock private FriendService friendService;
    @InjectMocks private GroupService groupService;

    private ChatGroup group;
    private GroupMember ownerMember;
    private User alice, bob, carol;

    @BeforeEach
    void setUp() {
        alice = buildUser(1L, "alice", "Alice");
        bob = buildUser(2L, "bob", "Bob");
        carol = buildUser(3L, "carol", "Carol");

        group = new ChatGroup();
        group.setId(100L);
        group.setName("Test Group");
        group.setOwnerId(1L);
        group.setDescription("A test group");

        ownerMember = new GroupMember();
        ownerMember.setId(1L);
        ownerMember.setGroupId(100L);
        ownerMember.setUserId(1L);
        ownerMember.setRole("OWNER");
    }

    @Nested
    @DisplayName("创建群聊")
    class Create {

        @Test
        @DisplayName("正常创建群聊并邀请好友")
        void shouldCreateGroupWithMembers() {
            CreateGroupRequest req = new CreateGroupRequest("Test", null, "desc", List.of(2L, 3L));
            when(userMapper.selectById(2L)).thenReturn(bob);
            when(userMapper.selectById(3L)).thenReturn(carol);
            when(friendService.isFriend(1L, 2L)).thenReturn(true);
            when(friendService.isFriend(1L, 3L)).thenReturn(true);
            when(userMapper.selectById(1L)).thenReturn(alice);
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            GroupVO result = groupService.create(1L, req);

            assertThat(result.name()).isEqualTo("Test");
            verify(groupMapper).insert(any(ChatGroup.class));
        }

        @Test
        @DisplayName("邀请非好友加入群聊时抛异常")
        void shouldThrowWhenInvitingNonFriend() {
            CreateGroupRequest req = new CreateGroupRequest("Test", null, "desc", List.of(2L));
            when(userMapper.selectById(2L)).thenReturn(bob);
            when(friendService.isFriend(1L, 2L)).thenReturn(false);

            assertThatThrownBy(() -> groupService.create(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("只能邀请好友加入群聊");
        }

        @Test
        @DisplayName("邀请不存在的用户时抛异常")
        void shouldThrowWhenUserNotFound() {
            CreateGroupRequest req = new CreateGroupRequest("Test", null, "desc", List.of(999L));
            when(userMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> groupService.create(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessage("邀请用户不存在");
        }
    }

    @Nested
    @DisplayName("加入群聊")
    class Join {

        @Test
        @DisplayName("正常加入")
        void shouldJoinGroup() {
            when(groupMapper.selectById(100L)).thenReturn(group);
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            groupService.join(2L, 100L);

            verify(memberMapper).insert(any(GroupMember.class));
        }

        @Test
        @DisplayName("已在群聊中时抛异常")
        void shouldThrowWhenAlreadyMember() {
            when(groupMapper.selectById(100L)).thenReturn(group);
            GroupMember existing = new GroupMember();
            existing.setGroupId(100L);
            existing.setUserId(2L);
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            assertThatThrownBy(() -> groupService.join(2L, 100L))
                .isInstanceOf(BizException.class)
                .hasMessage("已在群聊中");
        }
    }

    @Nested
    @DisplayName("退出群聊")
    class Leave {

        @Test
        @DisplayName("普通成员正常退出")
        void shouldLeave() {
            GroupMember member = new GroupMember();
            member.setId(5L);
            member.setGroupId(100L);
            member.setUserId(2L);
            member.setRole("MEMBER");
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

            groupService.leave(2L, 100L);

            verify(memberMapper).deleteById(5L);
        }

        @Test
        @DisplayName("群主不能直接退出")
        void shouldThrowWhenOwnerLeaves() {
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(ownerMember);

            assertThatThrownBy(() -> groupService.leave(1L, 100L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("群主不能直接退出");
        }
    }

    @Nested
    @DisplayName("移除成员")
    class RemoveMember {

        @Test
        @DisplayName("群主正常移除成员")
        void shouldRemoveMember() {
            when(groupMapper.selectById(100L)).thenReturn(group);

            groupService.removeMember(1L, 100L, 2L);

            verify(memberMapper).delete(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("非群主无法移除成员")
        void shouldThrowWhenNotOwner() {
            when(groupMapper.selectById(100L)).thenReturn(group);

            assertThatThrownBy(() -> groupService.removeMember(2L, 100L, 3L))
                .isInstanceOf(BizException.class)
                .hasMessage("只有群主可以移除成员");
        }

        @Test
        @DisplayName("不能移除群主自身")
        void shouldThrowWhenRemovingOwner() {
            when(groupMapper.selectById(100L)).thenReturn(group);

            assertThatThrownBy(() -> groupService.removeMember(1L, 100L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessage("不能移除群主");
        }
    }

    @Nested
    @DisplayName("requireMember / requireGroup")
    class Requirements {

        @Test
        @DisplayName("不是成员时抛异常")
        void shouldThrowWhenNotMember() {
            when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> groupService.requireMember(1L, 100L))
                .isInstanceOf(BizException.class)
                .hasMessage("不是群成员");
        }

        @Test
        @DisplayName("群聊不存在时抛异常")
        void shouldThrowWhenGroupNotFound() {
            when(groupMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> groupService.requireGroup(999L))
                .isInstanceOf(BizException.class)
                .hasMessage("群聊不存在");
        }
    }

    private User buildUser(Long id, String username, String nickname) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setNickname(nickname);
        u.setRole("USER");
        u.setStatus("ONLINE");
        return u;
    }
}
