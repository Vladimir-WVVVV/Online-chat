package com.whu.onlinechat.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MockAiProviderTest {
    private final MockAiProvider provider = new MockAiProvider();

    @Test
    void qaReturnsClassroomStyleAnswer() {
        String reply = provider.chat(AiAgentType.QA, "WebSocket 是什么", List.of());
        assertThat(reply).contains("答疑助手").contains("WebSocket");
    }

    @Test
    void summaryHandlesTooFewMessages() {
        String reply = provider.chat(AiAgentType.SUMMARY, "", List.of("Alice：你好"));
        assertThat(reply).isEqualTo("当前消息较少，暂无可总结内容");
    }
}
