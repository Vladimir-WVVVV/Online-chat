package com.whu.onlinechat.ai;

import com.whu.onlinechat.vo.MessageVO;
import java.util.List;

public record AiChatResponse(String agentType, String agentName, String content, MessageVO message,
                             List<MessageVO> messages) {
    public AiChatResponse(String agentType, String agentName, String content, MessageVO message) {
        this(agentType, agentName, content, message, List.of(message));
    }
}
