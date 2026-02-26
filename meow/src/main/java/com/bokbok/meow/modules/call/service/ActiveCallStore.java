package com.bokbok.meow.modules.call.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ActiveCallStore {

    private final StringRedisTemplate redisTemplate;

    // Key: "call:userId" → value: callId
    // Expires in 2 minutes (auto-cleanup if call hangs)
    private static final Duration CALL_TTL = Duration.ofMinutes(2);

    public void startCall(String userId, String callId) {
        redisTemplate.opsForValue()
                .set("call:" + userId, callId, CALL_TTL);
    }

    public boolean isOnCall(String userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("call:" + userId)
        );
    }

    public String getCallId(String userId) {
        return redisTemplate.opsForValue().get("call:" + userId);
    }

    public void endCall(String userId) {
        redisTemplate.delete("call:" + userId);
    }

    public void refreshCall(String userId) {
        redisTemplate.expire("call:" + userId, CALL_TTL);
    }
}