package com.whu.onlinechat.voice;

import com.whu.onlinechat.websocket.UserPrincipal;

public interface VoiceService {
    void forward(UserPrincipal user, VoiceSignalDTO signal, String forcedType);
}
