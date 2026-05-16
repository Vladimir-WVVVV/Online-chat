package com.whu.onlinechat.voice;

import java.time.LocalDateTime;
import java.util.Map;

public record VoiceSignalDTO(
    String type,
    Long fromUserId,
    Long toUserId,
    String fromUsername,
    Long targetUserId,
    String sdp,
    Map<String, Object> candidate,
    String message,
    LocalDateTime timestamp
) {
    public VoiceSignalDTO withSender(Long senderId, String username) {
        return new VoiceSignalDTO(type, senderId, toUserId, username, targetUserId, sdp, candidate, message, LocalDateTime.now());
    }

    public VoiceSignalDTO error(String errorMessage) {
        return new VoiceSignalDTO("ERROR", fromUserId, fromUserId, fromUsername, targetUserId, sdp, candidate, errorMessage, LocalDateTime.now());
    }
}
