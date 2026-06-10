package com.whu.onlinechat.ai;

import com.whu.onlinechat.common.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AiAgentType 单元测试")
class AiAgentTypeTest {

    @Nested
    @DisplayName("parse")
    class ParseTests {

        @Test
        @DisplayName("解析 QA 类型")
        void shouldParseQa() {
            assertThat(AiAgentType.parse("QA")).isEqualTo(AiAgentType.QA);
        }

        @Test
        @DisplayName("解析 SUMMARY 类型")
        void shouldParseSummary() {
            assertThat(AiAgentType.parse("SUMMARY")).isEqualTo(AiAgentType.SUMMARY);
        }

        @Test
        @DisplayName("解析 MOOD 类型")
        void shouldParseMood() {
            assertThat(AiAgentType.parse("MOOD")).isEqualTo(AiAgentType.MOOD);
        }

        @Test
        @DisplayName("null 值默认为 QA")
        void shouldDefaultToQaWhenNull() {
            assertThat(AiAgentType.parse(null)).isEqualTo(AiAgentType.QA);
        }

        @Test
        @DisplayName("空字符串默认为 QA")
        void shouldDefaultToQaWhenBlank() {
            assertThat(AiAgentType.parse("  ")).isEqualTo(AiAgentType.QA);
        }

        @Test
        @DisplayName("未知类型抛异常")
        void shouldThrowWhenUnknown() {
            assertThatThrownBy(() -> AiAgentType.parse("UNKNOWN"))
                .isInstanceOf(BizException.class)
                .hasMessage("未知 AI 助手类型");
        }
    }

    @Nested
    @DisplayName("username")
    class UsernameTests {

        @Test
        @DisplayName("QA 对应 ai_qa 用户")
        void qaUsername() {
            assertThat(AiAgentType.QA.username()).isEqualTo("ai_qa");
        }

        @Test
        @DisplayName("SUMMARY 对应 ai_summary 用户")
        void summaryUsername() {
            assertThat(AiAgentType.SUMMARY.username()).isEqualTo("ai_summary");
        }

        @Test
        @DisplayName("MOOD 对应 ai_mood 用户")
        void moodUsername() {
            assertThat(AiAgentType.MOOD.username()).isEqualTo("ai_mood");
        }
    }

    @Test
    @DisplayName("displayName 返回中文名称")
    void shouldReturnChineseDisplayName() {
        assertThat(AiAgentType.QA.displayName()).isEqualTo("答疑助手");
        assertThat(AiAgentType.SUMMARY.displayName()).isEqualTo("总结助手");
        assertThat(AiAgentType.MOOD.displayName()).isEqualTo("氛围助手");
    }
}
