package com.whu.onlinechat.voice;

import com.whu.onlinechat.websocket.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceWebSocketController 单元测试")
class VoiceWebSocketControllerTest {

    @Mock private VoiceService voiceService;
    @InjectMocks private VoiceWebSocketController controller;
    private final UserPrincipal alice = new UserPrincipal(1L, "alice", "USER");
    private final VoiceSignalDTO signal = new VoiceSignalDTO(null, 1L, 2L, "alice", 2L, null, null, null, null);

    @Test
    @DisplayName("发起呼叫转发为 CALL")
    void shouldForwardCall() {
        controller.call(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("CALL"));
    }

    @Test
    @DisplayName("接受呼叫转发为 ACCEPT")
    void shouldForwardAccept() {
        controller.accept(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("ACCEPT"));
    }

    @Test
    @DisplayName("拒绝呼叫转发为 REJECT")
    void shouldForwardReject() {
        controller.reject(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("REJECT"));
    }

    @Test
    @DisplayName("Offer 转发为 OFFER")
    void shouldForwardOffer() {
        controller.offer(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("OFFER"));
    }

    @Test
    @DisplayName("Answer 转发为 ANSWER")
    void shouldForwardAnswer() {
        controller.answer(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("ANSWER"));
    }

    @Test
    @DisplayName("ICE 转发为 ICE")
    void shouldForwardIce() {
        controller.ice(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("ICE"));
    }

    @Test
    @DisplayName("挂断转发为 HANGUP")
    void shouldForwardHangup() {
        controller.hangup(alice, signal);
        verify(voiceService).forward(eq(alice), any(), eq("HANGUP"));
    }
}
