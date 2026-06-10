package com.whu.onlinechat.controller;

import com.whu.onlinechat.ai.*;
import com.whu.onlinechat.common.ApiResult;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.vo.MessageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiController 单元测试")
class AiControllerTest {

    @Mock private AiService aiService;
    @InjectMocks private AiController aiController;
    private final CurrentUser alice = new CurrentUser(1L, "alice", "USER");

    @Nested
    @DisplayName("AI 聊天")
    class Chat {

        @Test
        @DisplayName("正常调用 AI 答疑")
        void shouldChat() {
            var req = new AiChatRequest("PRIVATE", 2L, "QA", "WebSocket是什么");
            var msg = new MessageVO(1L, "PRIVATE", 7L, "答疑助手", 1L, 2L,
                "答复内容", "TEXT", null, false, LocalDateTime.now());
            var resp = new AiChatResponse("QA", "答疑助手", "答复内容", msg);
            when(aiService.chat(eq(1L), any())).thenReturn(resp);

            ApiResult<AiChatResponse> result = aiController.chat(alice, req);

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.data().agentName()).isEqualTo("答疑助手");
            assertThat(result.data().content()).isEqualTo("答复内容");
        }
    }

    @Nested
    @DisplayName("AI 总结")
    class Summary {

        @Test
        @DisplayName("正常调用 AI 总结")
        void shouldSummary() {
            var req = new AiChatRequest("PRIVATE", 2L, "SUMMARY", "");
            var msg = new MessageVO(1L, "PRIVATE", 8L, "总结助手", 1L, 2L,
                "总结内容", "TEXT", null, false, LocalDateTime.now());
            var resp = new AiChatResponse("SUMMARY", "总结助手", "总结内容", msg);
            when(aiService.summary(eq(1L), any())).thenReturn(resp);

            var result = aiController.summary(alice, req);

            assertThat(result.data().agentName()).isEqualTo("总结助手");
        }
    }

    @Nested
    @DisplayName("AI 氛围")
    class Mood {

        @Test
        @DisplayName("正常调用 AI 氛围")
        void shouldMood() {
            var req = new AiChatRequest("GROUP", 100L, "MOOD", "");
            var msg = new MessageVO(1L, "GROUP", 9L, "氛围助手", null, 100L,
                "氛围回复", "TEXT", null, false, LocalDateTime.now());
            var resp = new AiChatResponse("MOOD", "氛围助手", "氛围回复", msg);
            when(aiService.mood(eq(1L), any())).thenReturn(resp);

            var result = aiController.mood(alice, req);

            assertThat(result.data().agentName()).isEqualTo("氛围助手");
            verify(aiService).mood(eq(1L), any());
        }
    }
}
