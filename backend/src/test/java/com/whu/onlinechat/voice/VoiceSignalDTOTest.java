package com.whu.onlinechat.voice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VoiceSignalDTOTest {
    @Test
    void withSenderFillsSenderAndTimestamp() {
        VoiceSignalDTO signal = new VoiceSignalDTO("CALL", null, 2L, null, 2L, null, null, null, null)
            .withSender(1L, "alice");

        assertThat(signal.fromUserId()).isEqualTo(1L);
        assertThat(signal.fromUsername()).isEqualTo("alice");
        assertThat(signal.timestamp()).isNotNull();
    }
}
