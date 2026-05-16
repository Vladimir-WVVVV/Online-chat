package com.whu.onlinechat.ai;

public interface AiService {
    AiChatResponse chat(Long userId, AiChatRequest request);

    AiChatResponse summary(Long userId, AiChatRequest request);

    AiChatResponse mood(Long userId, AiChatRequest request);
}
