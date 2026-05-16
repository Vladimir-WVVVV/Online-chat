package com.whu.onlinechat.voice;

import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.service.FriendService;
import com.whu.onlinechat.service.OnlineUserService;
import com.whu.onlinechat.service.UserService;
import com.whu.onlinechat.websocket.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {
    private final UserService userService;
    private final FriendService friendService;
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void forward(UserPrincipal user, VoiceSignalDTO signal, String forcedType) {
        userService.requireActiveUser(user.id());
        Long toUserId = signal.toUserId() != null ? signal.toUserId() : signal.targetUserId();
        if (toUserId == null) {
            sendError(user.id(), "语音通话目标不能为空");
            return;
        }
        if (!friendService.isFriend(user.id(), toUserId)) {
            sendError(user.id(), "只能和好友进行语音通话");
            return;
        }
        if (!onlineUserService.isOnline(toUserId)) {
            sendError(user.id(), "对方不在线或连接失败");
            return;
        }
        VoiceSignalDTO outbound = new VoiceSignalDTO(
            forcedType,
            user.id(),
            toUserId,
            user.username(),
            toUserId,
            signal.sdp(),
            signal.candidate(),
            signal.message(),
            java.time.LocalDateTime.now()
        );
        messagingTemplate.convertAndSendToUser(String.valueOf(toUserId), "/queue/voice", outbound);
    }

    private void sendError(Long userId, String message) {
        VoiceSignalDTO error = new VoiceSignalDTO("ERROR", userId, userId, null, userId, null, null, message, java.time.LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/voice", error);
    }
}
