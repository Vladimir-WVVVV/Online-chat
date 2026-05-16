package com.whu.onlinechat.ai;

import java.util.List;

public interface AiProvider {
    String chat(AiAgentType agentType, String userContent, List<String> recentMessages);
}
