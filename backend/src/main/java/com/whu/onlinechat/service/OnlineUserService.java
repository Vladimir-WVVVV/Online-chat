package com.whu.onlinechat.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnlineUserService {
    private final Map<Long, Set<String>> sessions = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;

    public void online(Long userId, String sessionId) {
        sessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(sessionId);
        try {
            redisTemplate.opsForSet().add("onlinechat:online:" + userId, sessionId);
        } catch (Exception ignored) {
            // Redis is best-effort; in-memory sessions keep classroom demo stable.
        }
    }

    public void offline(Long userId, String sessionId) {
        Set<String> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(sessionId);
            if (userSessions.isEmpty()) {
                sessions.remove(userId);
            }
        }
        try {
            redisTemplate.opsForSet().remove("onlinechat:online:" + userId, sessionId);
        } catch (Exception ignored) {
            // Redis is best-effort; in-memory sessions keep classroom demo stable.
        }
    }

    public boolean isOnline(Long userId) {
        return sessions.containsKey(userId);
    }

    public long onlineCount() {
        return sessions.size();
    }
}
