package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.CreateGroupRequest;
import com.whu.onlinechat.entity.ChatGroup;
import com.whu.onlinechat.entity.GroupMember;
import com.whu.onlinechat.entity.Notification;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.ChatGroupMapper;
import com.whu.onlinechat.mapper.GroupMemberMapper;
import com.whu.onlinechat.mapper.NotificationMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.GroupMemberVO;
import com.whu.onlinechat.vo.GroupVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final ChatGroupMapper groupMapper;
    private final GroupMemberMapper memberMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    @Transactional
    public GroupVO create(Long userId, CreateGroupRequest request) {
        userService.requireActiveUser(userId);
        ChatGroup group = new ChatGroup();
        group.setName(request.name());
        group.setOwnerId(userId);
        group.setAvatarUrl(request.avatarUrl());
        group.setDescription(request.description());
        groupMapper.insert(group);
        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setRole("OWNER");
        memberMapper.insert(owner);
        return toVO(group, userId);
    }

    public List<GroupVO> mine(Long userId) {
        userService.requireActiveUser(userId);
        return memberMapper.selectList(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getUserId, userId))
            .stream().map(m -> toVO(groupMapper.selectById(m.getGroupId()), userId)).toList();
    }

    public GroupVO detail(Long userId, Long groupId) {
        requireMember(userId, groupId);
        return toVO(requireGroup(groupId), userId);
    }

    public void join(Long userId, Long groupId) {
        userService.requireActiveUser(userId);
        requireGroup(groupId);
        GroupMember existing = memberMapper.selectOne(new LambdaQueryWrapper<GroupMember>()
            .eq(GroupMember::getGroupId, groupId)
            .eq(GroupMember::getUserId, userId));
        if (existing != null) {
            existing.setDeleted(0);
            memberMapper.updateById(existing);
            return;
        }
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        memberMapper.insert(member);
    }

    public void leave(Long userId, Long groupId) {
        GroupMember member = requireMember(userId, groupId);
        if ("OWNER".equals(member.getRole())) {
            throw new BizException("群主不能直接退出，请先移除群成员后保留群聊用于演示");
        }
        memberMapper.deleteById(member.getId());
    }

    public List<GroupMemberVO> members(Long userId, Long groupId) {
        requireMember(userId, groupId);
        return memberMapper.selectList(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId))
            .stream().map(m -> {
                User u = userMapper.selectById(m.getUserId());
                return new GroupMemberVO(u.getId(), u.getUsername(), u.getNickname(), u.getAvatarUrl(), u.getStatus(),
                    m.getRole(), m.getJoinedTime());
            }).toList();
    }

    public void removeMember(Long ownerId, Long groupId, Long userId) {
        ChatGroup group = requireGroup(groupId);
        if (!ownerId.equals(group.getOwnerId())) {
            throw new BizException("只有群主可以移除成员");
        }
        if (ownerId.equals(userId)) {
            throw new BizException("不能移除群主");
        }
        memberMapper.delete(new LambdaQueryWrapper<GroupMember>()
            .eq(GroupMember::getGroupId, groupId)
            .eq(GroupMember::getUserId, userId));
    }

    public GroupMember requireMember(Long userId, Long groupId) {
        userService.requireActiveUser(userId);
        GroupMember member = memberMapper.selectOne(new LambdaQueryWrapper<GroupMember>()
            .eq(GroupMember::getUserId, userId)
            .eq(GroupMember::getGroupId, groupId));
        if (member == null) {
            throw new BizException("不是群成员");
        }
        return member;
    }

    public ChatGroup requireGroup(Long groupId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BizException("群聊不存在");
        }
        return group;
    }

    public List<Long> memberIds(Long groupId) {
        return memberMapper.selectList(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId))
            .stream().map(GroupMember::getUserId).toList();
    }

    private GroupVO toVO(ChatGroup group, Long viewerId) {
        User owner = userMapper.selectById(group.getOwnerId());
        Long members = memberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, group.getId()));
        Long unread = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getReceiverId, viewerId)
            .eq(Notification::getReadFlag, 0)
            .eq(Notification::getType, "GROUP_MESSAGE")
            .like(Notification::getContent, "群 " + group.getId() + " "));
        return new GroupVO(group.getId(), group.getName(), group.getOwnerId(), owner.getNickname(), group.getAvatarUrl(),
            group.getDescription(), members, unread, group.getCreateTime());
    }
}
