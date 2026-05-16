package com.whu.onlinechat.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpAiProvider implements AiProvider {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public HttpAiProvider(
        @Value("${AI_API_BASE_URL:${ai.api-base-url:}}") String baseUrl,
        @Value("${AI_API_KEY:${ai.api-key:}}") String apiKey,
        @Value("${AI_MODEL:${ai.model:}}") String model
    ) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean available() {
        return baseUrl != null && !baseUrl.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String chat(AiAgentType agentType, String userContent, List<String> recentMessages) {
        if (!available()) {
            throw new IllegalStateException("HTTP AI 未配置");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model == null || model.isBlank() ? "default" : model);
        body.put("agentType", agentType.name());
        body.put("prompt", buildPrompt(agentType, userContent, recentMessages));

        Map<String, Object> response = restTemplate.postForObject(baseUrl, new HttpEntity<>(body, headers), Map.class);
        if (response == null) {
            throw new IllegalStateException("HTTP AI 响应为空");
        }
        Object content = response.get("content");
        if (content == null) {
            Object text = response.get("text");
            content = text;
        }
        if (content == null) {
            throw new IllegalStateException("HTTP AI 响应缺少 content/text 字段");
        }
        return String.valueOf(content);
    }

    private String buildPrompt(AiAgentType agentType, String userContent, List<String> recentMessages) {
        return "你是" + agentType.displayName() + "。用户输入：" + (userContent == null ? "" : userContent)
            + "\n最近消息：\n" + String.join("\n", recentMessages == null ? List.of() : recentMessages);
    }
}
