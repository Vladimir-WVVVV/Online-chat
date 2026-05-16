package com.whu.onlinechat.voice;

import com.whu.onlinechat.websocket.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class VoiceWebSocketController {
    private final VoiceService voiceService;

    @MessageMapping("/voice.call")
    public void call(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "CALL");
    }

    @MessageMapping("/voice.accept")
    public void accept(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "ACCEPT");
    }

    @MessageMapping("/voice.reject")
    public void reject(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "REJECT");
    }

    @MessageMapping("/voice.offer")
    public void offer(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "OFFER");
    }

    @MessageMapping("/voice.answer")
    public void answer(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "ANSWER");
    }

    @MessageMapping("/voice.ice")
    public void ice(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "ICE");
    }

    @MessageMapping("/voice.hangup")
    public void hangup(UserPrincipal user, @Payload VoiceSignalDTO signal) {
        voiceService.forward(user, signal, "HANGUP");
    }
}
