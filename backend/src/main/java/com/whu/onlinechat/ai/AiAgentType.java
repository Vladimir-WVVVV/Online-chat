package com.whu.onlinechat.ai;

import com.whu.onlinechat.common.BizException;

public enum AiAgentType {
    QA("ai_qa", "答疑助手"),
    SUMMARY("ai_summary", "总结助手"),
    MOOD("ai_mood", "氛围助手");

    private final String username;
    private final String displayName;

    AiAgentType(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public String username() {
        return username;
    }

    public String displayName() {
        return displayName;
    }

    public static AiAgentType parse(String value) {
        if (value == null || value.isBlank()) {
            return QA;
        }
        try {
            return AiAgentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException("未知 AI 助手类型");
        }
    }
}
