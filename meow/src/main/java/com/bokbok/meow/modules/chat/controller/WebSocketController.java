package com.bokbok.meow.modules.chat.controller;

import com.bokbok.meow.modules.chat.dto.SendMessageRequest;
import com.bokbok.meow.modules.chat.dto.TypingEvent;
import com.bokbok.meow.modules.chat.service.ChatService;
import com.bokbok.meow.websocket.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
@Controller
@Slf4j
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    // Client sends to /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request,
                            Principal principal) {
        if (principal == null) {
            log.warn("Message rejected: unauthenticated WebSocket session");
            return;
        }
        chatService.sendMessage(principal.getName(), request);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingEvent event,
                             Principal principal) {
        if (principal == null) return;
        event.setSenderId(principal.getName());
        messagingTemplate.convertAndSendToUser(
                event.getReceiverId(),
                "/queue/typing",
                event
        );
    }

    @MessageMapping("/chat.delivered")
    public void markDelivered(Principal principal) {
        if (principal == null) return;
        chatService.markDelivered(principal.getName());
        presenceService.refreshPresence(principal.getName());
    }
}