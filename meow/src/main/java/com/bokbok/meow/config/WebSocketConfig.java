package com.bokbok.meow.config;
import lombok.extern.slf4j.Slf4j;
import com.bokbok.meow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients subscribe to topics with /topic or /queue prefix
        registry.enableSimpleBroker("/topic", "/queue");
        // Client sends messages to /app prefix
        registry.setApplicationDestinationPrefixes("/app");
        // User-specific messages prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // fallback for browsers
    }

    // Intercept WebSocket CONNECT and validate JWT
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor
                        .getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // Try Authorization header first
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    // Fallback: try "token" header (some clients send it this way)
                    if (authHeader == null) {
                        String rawToken = accessor.getFirstNativeHeader("token");
                        if (rawToken != null) {
                            authHeader = "Bearer " + rawToken;
                        }
                    }

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        if (jwtUtil.isTokenValid(token)) {
                            String userId = jwtUtil.extractUserId(token);

                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userId, null, Collections.emptyList()
                                    );

                            SecurityContextHolder.getContext().setAuthentication(auth);
                            accessor.setUser(auth);
                            log.info("WebSocket authenticated: {}", userId);
                        } else {
                            log.warn("WebSocket CONNECT rejected: invalid token");
                        }
                    } else {
                        log.warn("WebSocket CONNECT has no Authorization header");
                    }
                }
                return message;
            }
        });
    }
}