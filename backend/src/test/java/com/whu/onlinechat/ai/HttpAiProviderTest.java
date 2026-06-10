package com.whu.onlinechat.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class HttpAiProviderTest {
    @Test
    void availableWhenHttpConfigIsComplete() {
        HttpAiProvider provider = new HttpAiProvider(new RestTemplate(), new ObjectMapper(),
            "https://open.bigmodel.cn/api/paas/v4/chat/completions", "test-key", "glm-4.5-flash");

        assertThat(provider.available()).isTrue();
    }

    @Test
    void unavailableWhenApiKeyMissing() {
        HttpAiProvider provider = new HttpAiProvider(new RestTemplate(), new ObjectMapper(),
            "https://open.bigmodel.cn/api/paas/v4/chat/completions", "", "glm-4.5-flash");

        assertThat(provider.available()).isFalse();
        assertThat(provider.unavailableReason()).isEqualTo("AI_API_KEY is empty");
    }

    @Test
    void parsesOpenAiCompatibleChatCompletionContent() {
        HttpAiProvider provider = new HttpAiProvider(new RestTemplate(), new ObjectMapper(),
            "https://open.bigmodel.cn/api/paas/v4/chat/completions", "test-key", "glm-4.5-flash");

        String content = provider.parseContent("""
            {
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "我是 OnlineChat 项目中的 AI 助手。"
                  }
                }
              ]
            }
            """);

        assertThat(content).isEqualTo("我是 OnlineChat 项目中的 AI 助手。");
    }
}
