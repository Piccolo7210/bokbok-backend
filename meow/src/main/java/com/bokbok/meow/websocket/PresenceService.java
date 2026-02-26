package com.bokbok.meow.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redisTemplate;

    private static final String PRESENCE_KEY = "presence:";
    private static final Duration PRESENCE_TTL = Duration.ofMinutes(2);

    public void setOnline(String userId) {
        redisTemplate.opsForValue()
                .set(PRESENCE_KEY + userId, "ONLINE", PRESENCE_TTL);
    }

    public void setOffline(String userId) {
        redisTemplate.delete(PRESENCE_KEY + userId);
    }

    public boolean isOnline(String userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PRESENCE_KEY + userId)
        );
    }

    // Refresh TTL — call this on every message/activity
    public void refreshPresence(String userId) {
        redisTemplate.expire(PRESENCE_KEY + userId, PRESENCE_TTL);
    }
}