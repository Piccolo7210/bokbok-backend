package com.bokbok.meow.websocket;

import com.bokbok.meow.modules.user.entity.User;
import com.bokbok.meow.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() != null) {
            String userId = accessor.getUser().getName();
            presenceService.setOnline(userId);

            // Update DB status
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(User.UserStatus.ONLINE);
                userRepository.save(user);
            });

            log.info("User connected: {}", userId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() != null) {
            String userId = accessor.getUser().getName();
            presenceService.setOffline(userId);

            // Update DB status and lastSeen
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(User.UserStatus.OFFLINE);
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);
            });

            // Notify all relevant users this person went offline
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/presence",
                    Map.of("userId", userId, "status", "OFFLINE")
            );

            log.info("User disconnected: {}", userId);
        }
    }
}