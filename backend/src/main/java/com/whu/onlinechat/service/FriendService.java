package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.FriendRequestCreateRequest;
import com.whu.onlinechat.dto.RemarkRequest;
import com.whu.onlinechat.entity.FriendRequest;
import com.whu.onlinechat.entity.Friendship;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.FriendRequestMapper;
import com.whu.onlinechat.mapper.FriendshipMapper;
import com.whu.onlinechat.mapper.NotificationMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.FriendRequestVO;
import com.whu.onlinechat.vo.FriendVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendshipMapper friendshipMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final NotificationService notificationService;
    private final UserService userService;
    private final OnlineUserService onlineUserService;

    public List<FriendVO> list(Long userId) {
        userService.requireActiveUser(userId);
        return friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, userId)
                .eq(Friendship::getStatus, "ACTIVE"))
            .stream().map(f -> {
                User friend = userMapper.selectById(f.getFriendId());
                Long unread = notificationMapper.selectCount(new LambdaQueryWrapper<com.whu.onlinechat.entity.Notification>()
                    .eq(com.whu.onlinechat.entity.Notification::getReceiverId, userId)
                    .eq(com.whu.onlinechat.entity.Notification::getReadFlag, 0)
                    .eq(com.whu.onlinechat.entity.Notification::getType, "PRIVATE_MESSAGE")
                    .like(com.whu.onlinechat.entity.Notification::getContent, "用户 " + f.getFriendId() + " "));
                String status = onlineUserService.isOnline(f.getFriendId()) ? "ONLINE" : friend.getStatus();
                return new FriendVO(friend.getId(), friend.getUsername(), friend.getNickname(), friend.getAvatarUrl(),
                    friend.getBio(), status, f.getRemark(), unread);
            }).toList();
    }

    @Transactional
    public void request(Long userId, FriendRequestCreateRequest request) {
        userService.requireActiveUser(userId);
        if (userId.equals(request.toUserId())) {
            throw new BizException("不能添加自己");
        }
        User target = userMapper.selectById(request.toUserId());
        if (target == null) {
            throw new BizException("用户不存在");
        }
        if (isFriend(userId, request.toUserId())) {
            throw new BizException("已经是好友");
        }
        Long pending = friendRequestMapper.selectCount(new LambdaQueryWrapper<FriendRequest>()
            .eq(FriendRequest::getFromUserId, userId)
            .eq(FriendRequest::getToUserId, request.toUserId())
            .eq(FriendRequest::getStatus, "PENDING"));
        if (pending > 0) {
            throw new BizException("已有待处理申请");
        }
        FriendRequest entity = new FriendRequest();
        entity.setFromUserId(userId);
        entity.setToUserId(request.toUserId());
        entity.setStatus("PENDING");
        entity.setMessage(request.message());
        friendRequestMapper.insert(entity);
        notificationService.create(request.toUserId(), "FRIEND_REQUEST", "用户 " + userId + " 请求添加你为好友");
    }

    public List<FriendRequestVO> received(Long userId) {
        userService.requireActiveUser(userId);
        return friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .orderByDesc(FriendRequest::getCreateTime))
            .stream().map(this::toVO).toList();
    }

    public List<FriendRequestVO> sent(Long userId) {
        userService.requireActiveUser(userId);
        return friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getFromUserId, userId)
                .orderByDesc(FriendRequest::getCreateTime))
            .stream().map(this::toVO).toList();
    }

    @Transactional
    public void accept(Long userId, Long requestId) {
        FriendRequest request = requireRequestForUser(userId, requestId);
        if (!"PENDING".equals(request.getStatus())) {
            throw new BizException("申请已处理");
        }
        request.setStatus("ACCEPTED");
        friendRequestMapper.updateById(request);
        upsertFriendship(request.getFromUserId(), request.getToUserId());
        upsertFriendship(request.getToUserId(), request.getFromUserId());
        notificationService.create(request.getFromUserId(), "FRIEND_ACCEPTED", "用户 " + userId + " 已接受好友申请");
    }

    @Transactional
    public void reject(Long userId, Long requestId) {
        FriendRequest request = requireRequestForUser(userId, requestId);
        if (!"PENDING".equals(request.getStatus())) {
            throw new BizException("申请已处理");
        }
        request.setStatus("REJECTED");
        friendRequestMapper.updateById(request);
    }

    public void remark(Long userId, Long friendId, RemarkRequest request) {
        int updated = friendshipMapper.update(new Friendship(), new LambdaUpdateWrapper<Friendship>()
            .eq(Friendship::getUserId, userId)
            .eq(Friendship::getFriendId, friendId)
            .set(Friendship::getRemark, request.remark()));
        if (updated == 0) {
            throw new BizException("好友不存在");
        }
    }

    public void delete(Long userId, Long friendId) {
        friendshipMapper.update(new Friendship(), new LambdaUpdateWrapper<Friendship>()
            .eq(Friendship::getUserId, userId).eq(Friendship::getFriendId, friendId)
            .set(Friendship::getStatus, "DELETED"));
        friendshipMapper.update(new Friendship(), new LambdaUpdateWrapper<Friendship>()
            .eq(Friendship::getUserId, friendId).eq(Friendship::getFriendId, userId)
            .set(Friendship::getStatus, "DELETED"));
    }

    public boolean isFriend(Long userId, Long friendId) {
        return friendshipMapper.selectCount(new LambdaQueryWrapper<Friendship>()
            .eq(Friendship::getUserId, userId)
            .eq(Friendship::getFriendId, friendId)
            .eq(Friendship::getStatus, "ACTIVE")) > 0;
    }

    private FriendRequest requireRequestForUser(Long userId, Long requestId) {
        userService.requireActiveUser(userId);
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !userId.equals(request.getToUserId())) {
            throw new BizException("好友申请不存在");
        }
        return request;
    }

    private void upsertFriendship(Long userId, Long friendId) {
        Friendship existing = friendshipMapper.selectOne(new LambdaQueryWrapper<Friendship>()
            .eq(Friendship::getUserId, userId)
            .eq(Friendship::getFriendId, friendId));
        if (existing == null) {
            Friendship f = new Friendship();
            f.setUserId(userId);
            f.setFriendId(friendId);
            f.setStatus("ACTIVE");
            friendshipMapper.insert(f);
        } else {
            existing.setStatus("ACTIVE");
            existing.setDeleted(0);
            friendshipMapper.updateById(existing);
        }
    }

    private FriendRequestVO toVO(FriendRequest request) {
        User from = userMapper.selectById(request.getFromUserId());
        User to = userMapper.selectById(request.getToUserId());
        return new FriendRequestVO(request.getId(), request.getFromUserId(), from.getUsername(), from.getNickname(),
            request.getToUserId(), to.getUsername(), to.getNickname(), request.getStatus(), request.getMessage(),
            request.getCreateTime());
    }
}
