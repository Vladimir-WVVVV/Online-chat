package com.whu.onlinechat.voice;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.onlinechat.service.FriendService;
import com.whu.onlinechat.service.OnlineUserService;
import com.whu.onlinechat.service.UserService;
import com.whu.onlinechat.websocket.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class VoiceServiceImplTest {
    @Mock
    UserService userService;
    @Mock
    FriendService friendService;
    @Mock
    OnlineUserService onlineUserService;
    @Mock
    SimpMessagingTemplate messagingTemplate;
    @InjectMocks
    VoiceServiceImpl voiceService;

    @Test
    void nonFriendCannotStartVoice() {
        UserPrincipal alice = new UserPrincipal(1L, "alice", "USER");
        VoiceSignalDTO signal = new VoiceSignalDTO("CALL", null, 2L, null, 2L, null, null, null, null);
        when(friendService.isFriend(1L, 2L)).thenReturn(false);

        voiceService.forward(alice, signal, "CALL");

        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/voice"), any(VoiceSignalDTO.class));
        verify(messagingTemplate, never()).convertAndSendToUser(eq("2"), eq("/queue/voice"), any(VoiceSignalDTO.class));
    }
}
