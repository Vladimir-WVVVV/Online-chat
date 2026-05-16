package com.whu.onlinechat.ai;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockAiProvider implements AiProvider {
    @Override
    public String chat(AiAgentType agentType, String userContent, List<String> recentMessages) {
        String content = userContent == null ? "" : userContent.trim();
        return switch (agentType) {
            case QA -> "【答疑助手】关于“" + blankFallback(content, "这个问题") + "”：可以先抓住核心概念、适用场景和常见误区。"
                + " 如果以 WebSocket 为例，它是在一次 HTTP 握手后建立的双向长连接，适合实时聊天、通知和协同场景。";
            case SUMMARY -> summary(recentMessages);
            case MOOD -> "【氛围助手】我看最近聊天节奏还不错，可以继续围绕刚才的问题补一个例子，或者请一位同学用一句话复述重点。";
        };
    }

    private String summary(List<String> recentMessages) {
        if (recentMessages == null || recentMessages.size() < 3) {
            return "当前消息较少，暂无可总结内容";
        }
        return "【总结助手】最近聊天小结：\n1. 大家主要围绕当前会话中的问题进行讨论。\n2. 关键信息集中在最近 "
            + recentMessages.size() + " 条有效消息中。\n3. 建议下一步明确待解决问题，并把结论沉淀为一条可执行事项。";
    }

    private String blankFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
