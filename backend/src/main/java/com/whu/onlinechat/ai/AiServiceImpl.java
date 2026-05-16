package com.whu.onlinechat.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.Message;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.MessageMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.service.FriendService;
import com.whu.onlinechat.service.GroupService;
import com.whu.onlinechat.service.UserService;
import com.whu.onlinechat.vo.MessageVO;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
    private final MockAiProvider mockAiProvider;
    private final HttpAiProvider httpAiProvider;
    private final UserService userService;
    private final FriendService friendService;
    private final GroupService groupService;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${AI_PROVIDER:${ai.provider:mock}}")
    private String provider;

    @Override
    @Transactional
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        return handle(userId, request, AiAgentType.parse(request.agentType()), request.content());
    }

    @Override
    @Transactional
    public AiChatResponse summary(Long userId, AiChatRequest request) {
        return handle(userId, request, AiAgentType.SUMMARY, "请总结最近聊天内容");
    }

    @Override
    @Transactional
    public AiChatResponse mood(Long userId, AiChatRequest request) {
        return handle(userId, request, AiAgentType.MOOD, "请根据最近聊天内容给出轻量互动回复");
    }

    private AiChatResponse handle(Long userId, AiChatRequest request, AiAgentType agentType, String content) {
        userService.requireActiveUser(userId);
        String conversationType = normalizeConversationType(request.conversationType());
        validateConversation(userId, request.targetId(), conversationType);
        List<String> recentMessages = recentMessages(userId, request.targetId(), conversationType);
        if (agentType == AiAgentType.SUMMARY && recentMessages.size() < 3) {
            content = "当前消息较少，暂无可总结内容";
        } else {
            content = callProvider(agentType, content, recentMessages);
        }
        MessageVO message = saveAndPush(userId, request.targetId(), conversationType, agentType, content);
        return new AiChatResponse(agentType.name(), agentType.displayName(), content, message);
    }

    private String callProvider(AiAgentType agentType, String content, List<String> recentMessages) {
        if ("http".equalsIgnoreCase(provider)) {
            try {
                return httpAiProvider.chat(agentType, content, recentMessages);
            } catch (Exception ignored) {
                return mockAiProvider.chat(agentType, content, recentMessages);
            }
        }
        return mockAiProvider.chat(agentType, content, recentMessages);
    }

    private void validateConversation(Long userId, Long targetId, String conversationType) {
        if (targetId == null) {
            throw new BizException("会话目标不能为空");
        }
        if ("PRIVATE".equals(conversationType)) {
            if (!friendService.isFriend(userId, targetId)) {
                throw new BizException("只能在好友私聊中使用 AI");
            }
        } else {
            groupService.requireMember(userId, targetId);
        }
    }

    private List<String> recentMessages(Long userId, Long targetId, String conversationType) {
        List<Message> messages;
        if ("PRIVATE".equals(conversationType)) {
            messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationType, "PRIVATE")
                .eq(Message::getRecalled, 0)
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetId)
                    .or().eq(Message::getSenderId, targetId).eq(Message::getReceiverId, userId)
                    .or().eq(Message::getReceiverId, userId).eq(Message::getGroupId, targetId))
                .orderByDesc(Message::getCreateTime)
                .last("limit 20"));
        } else {
            messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationType, "GROUP")
                .eq(Message::getGroupId, targetId)
                .eq(Message::getRecalled, 0)
                .orderByDesc(Message::getCreateTime)
                .last("limit 20"));
        }
        Collections.reverse(messages);
        return messages.stream()
            .filter(message -> !isAiUser(message.getSenderId()))
            .map(message -> senderName(message.getSenderId()) + "：" + message.getContent())
            .toList();
    }

    private MessageVO saveAndPush(Long userId, Long targetId, String conversationType, AiAgentType agentType, String content) {
        User aiUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, agentType.username()));
        if (aiUser == null) {
            throw new BizException("AI 虚拟用户未初始化");
        }
        Message message = new Message();
        message.setConversationType(conversationType);
        message.setSenderId(aiUser.getId());
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setRecalled(0);
        if ("PRIVATE".equals(conversationType)) {
            message.setReceiverId(userId);
            message.setGroupId(targetId);
        } else {
            message.setGroupId(targetId);
        }
        messageMapper.insert(message);
        MessageVO vo = MessageVO.of(message, agentType.displayName());
        if ("PRIVATE".equals(conversationType)) {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/messages", vo);
        } else {
            messagingTemplate.convertAndSend("/topic/groups/" + targetId, vo);
        }
        return vo;
    }

    private String normalizeConversationType(String conversationType) {
        String value = conversationType == null ? "" : conversationType.trim().toUpperCase();
        if (!"PRIVATE".equals(value) && !"GROUP".equals(value)) {
            throw new BizException("会话类型不正确");
        }
        return value;
    }

    private boolean isAiUser(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && "AI".equals(user.getRole());
    }

    private String senderName(Long senderId) {
        User user = userMapper.selectById(senderId);
        return user == null ? "未知用户" : user.getNickname();
    }
}
