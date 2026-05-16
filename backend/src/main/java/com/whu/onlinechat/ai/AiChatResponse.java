package com.whu.onlinechat.ai;

import com.whu.onlinechat.vo.MessageVO;

public record AiChatResponse(String agentType, String agentName, String content, MessageVO message) {
}
