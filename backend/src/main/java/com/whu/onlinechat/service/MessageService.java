package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.ChatMessageRequest;
import com.whu.onlinechat.entity.Message;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.MessageMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.MessageVO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final FriendService friendService;
    private final GroupService groupService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageVO sendPrivate(Long senderId, ChatMessageRequest request) {
        userService.requireActiveUser(senderId);
        if (request.receiverId() == null) {
            throw new BizException("接收人不能为空");
        }
        if (!friendService.isFriend(senderId, request.receiverId())) {
            throw new BizException("只能给好友发送私聊消息");
        }
        Message message = baseMessage(senderId, request);
        message.setConversationType("PRIVATE");
        message.setReceiverId(request.receiverId());
        messageMapper.insert(message);
        MessageVO vo = toVO(message);
        notificationService.create(request.receiverId(), "PRIVATE_MESSAGE", "用户 " + senderId + " 发来私聊消息");
        messagingTemplate.convertAndSendToUser(String.valueOf(request.receiverId()), "/queue/messages", vo);
        messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", vo);
        return vo;
    }

    @Transactional
    public MessageVO sendGroup(Long senderId, ChatMessageRequest request) {
        userService.requireActiveUser(senderId);
        if (request.groupId() == null) {
            throw new BizException("群聊不能为空");
        }
        groupService.requireMember(senderId, request.groupId());
        Message message = baseMessage(senderId, request);
        message.setConversationType("GROUP");
        message.setGroupId(request.groupId());
        messageMapper.insert(message);
        MessageVO vo = toVO(message);
        for (Long memberId : groupService.memberIds(request.groupId())) {
            if (!memberId.equals(senderId)) {
                notificationService.create(memberId, "GROUP_MESSAGE", "群 " + request.groupId() + " 收到新消息");
            }
        }
        messagingTemplate.convertAndSend("/topic/groups/" + request.groupId(), vo);
        return vo;
    }

    public List<MessageVO> privateHistory(Long userId, Long friendId, int page, int size, String keyword) {
        userService.requireActiveUser(userId);
        int offset = Math.max(page - 1, 0) * size;
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationType, "PRIVATE")
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, friendId)
                    .or().eq(Message::getSenderId, friendId).eq(Message::getReceiverId, userId)
                    .or().eq(Message::getReceiverId, userId).eq(Message::getGroupId, friendId))
                .like(keyword != null && !keyword.isBlank(), Message::getContent, keyword)
                .orderByDesc(Message::getCreateTime)
                .last("limit " + size + " offset " + offset))
            .stream().map(this::toVO).toList();
    }

    public List<MessageVO> groupHistory(Long userId, Long groupId, int page, int size, String keyword) {
        groupService.requireMember(userId, groupId);
        int offset = Math.max(page - 1, 0) * size;
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationType, "GROUP")
                .eq(Message::getGroupId, groupId)
                .like(keyword != null && !keyword.isBlank(), Message::getContent, keyword)
                .orderByDesc(Message::getCreateTime)
                .last("limit " + size + " offset " + offset))
            .stream().map(this::toVO).toList();
    }

    public List<MessageVO> search(Long userId, String keyword) {
        userService.requireActiveUser(userId);
        String text = keyword == null ? "" : keyword;
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .like(!text.isBlank(), Message::getContent, text)
                .and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId))
                .orderByDesc(Message::getCreateTime)
                .last("limit 50"))
            .stream().map(this::toVO).toList();
    }

    @Transactional
    public MessageVO recall(Long userId, Long messageId) {
        userService.requireActiveUser(userId);
        Message message = messageMapper.selectById(messageId);
        if (message == null || !userId.equals(message.getSenderId())) {
            throw new BizException("消息不存在或无权撤回");
        }
        if (message.getCreateTime() != null && message.getCreateTime().isBefore(LocalDateTime.now().minusMinutes(2))) {
            throw new BizException("只能撤回 2 分钟内的消息");
        }
        messageMapper.update(new Message(), new LambdaUpdateWrapper<Message>()
            .eq(Message::getId, messageId)
            .set(Message::getRecalled, 1)
            .set(Message::getContent, "消息已撤回"));
        message.setRecalled(1);
        message.setContent("消息已撤回");
        MessageVO vo = toVO(message);
        if ("PRIVATE".equals(message.getConversationType())) {
            messagingTemplate.convertAndSendToUser(String.valueOf(message.getReceiverId()), "/queue/messages", vo);
            messagingTemplate.convertAndSendToUser(String.valueOf(message.getSenderId()), "/queue/messages", vo);
        } else {
            messagingTemplate.convertAndSend("/topic/groups/" + message.getGroupId(), vo);
        }
        return vo;
    }

    public void readPrivate(Long userId, Long friendId) {
        userService.requireActiveUser(userId);
        notificationService.readByTypeAndKeyword(userId, "PRIVATE_MESSAGE", "用户 " + friendId + " ");
    }

    public void readGroup(Long userId, Long groupId) {
        userService.requireActiveUser(userId);
        notificationService.readByTypeAndKeyword(userId, "GROUP_MESSAGE", "群 " + groupId + " ");
    }

    private Message baseMessage(Long senderId, ChatMessageRequest request) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setContent(request.content());
        message.setMessageType(request.messageType() == null ? "TEXT" : request.messageType());
        message.setFileId(request.fileId());
        message.setRecalled(0);
        return message;
    }

    private MessageVO toVO(Message message) {
        User sender = userMapper.selectById(message.getSenderId());
        return MessageVO.of(message, sender == null ? "未知用户" : sender.getNickname());
    }
}
