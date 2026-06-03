package com.whu.onlinechat.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class HttpAiProvider implements AiProvider {
    private static final String SYSTEM_PROMPT = "你是 OnlineChat 在线聊天室项目中的 AI 助手。请直接回答用户问题，回答要自然、简洁、适合课程项目演示。不要套用固定模板。";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    @Autowired
    public HttpAiProvider(
        @Value("${AI_API_BASE_URL:${ai.api-base-url:}}") String baseUrl,
        @Value("${AI_API_KEY:${ai.api-key:}}") String apiKey,
        @Value("${AI_MODEL:${ai.model:}}") String model
    ) {
        this(new RestTemplate(), new ObjectMapper(), baseUrl, apiKey, model);
    }

    HttpAiProvider(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl, String apiKey, String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean available() {
        return baseUrlPresent() && keyPresent() && modelPresent();
    }

    public boolean baseUrlPresent() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    public boolean keyPresent() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String keyPreview() {
        if (!keyPresent()) {
            return "";
        }
        String trimmed = apiKey.trim();
        if (trimmed.length() <= 6) {
            return "****";
        }
        return trimmed.substring(0, 3) + "****" + trimmed.substring(trimmed.length() - 3);
    }

    public String model() {
        return model == null || model.isBlank() ? "" : model.trim();
    }

    public String unavailableReason() {
        if (!baseUrlPresent()) {
            return "AI_API_BASE_URL is empty";
        }
        if (!keyPresent()) {
            return "AI_API_KEY is empty";
        }
        if (!modelPresent()) {
            return "AI_MODEL is empty";
        }
        return "";
    }

    @Override
    public String chat(AiAgentType agentType, String userContent, List<String> recentMessages) {
        if (!available()) {
            throw new IllegalStateException(unavailableReason());
        }
        log.info("AI HTTP provider request config: model={}, baseUrlPresent={}, keyPresent={}, keyPreview={}",
            model(), baseUrlPresent(), keyPresent(), keyPreview());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model());
        body.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt(agentType, recentMessages)),
            Map.of("role", "user", "content", userContent == null ? "" : userContent)
        ));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            String content = parseContent(response.getBody());
            log.info("AI HTTP provider success, contentLength={}", content.length());
            return content;
        } catch (HttpStatusCodeException ex) {
            log.warn("AI HTTP request failed, statusCode={}, responseBody={}",
                ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("HTTP request failed, statusCode=" + ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("HTTP response parse/request failed: " + ex.getMessage(), ex);
        }
    }

    String parseContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("HTTP AI response body is empty");
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.isNull() || contentNode.asText().isBlank()) {
                throw new IllegalStateException("HTTP AI response missing choices[0].message.content");
            }
            return contentNode.asText();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("HTTP AI response JSON parse failed", ex);
        }
    }

    private String systemPrompt(AiAgentType agentType, List<String> recentMessages) {
        String recent = recentMessages == null || recentMessages.isEmpty()
            ? ""
            : "\n最近聊天记录：\n" + String.join("\n", recentMessages);
        return SYSTEM_PROMPT + "\n当前助手：" + agentType.displayName() + recent;
    }

    private boolean modelPresent() {
        return model != null && !model.isBlank();
    }
}
