package com.bokbok.meow.modules.chat.controller;

import com.bokbok.meow.modules.chat.dto.ConversationResponse;
import com.bokbok.meow.modules.chat.dto.MessageResponse;
import com.bokbok.meow.modules.chat.dto.SendMessageRequest;
import com.bokbok.meow.modules.chat.service.ChatService;
import com.bokbok.meow.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // POST /api/chat/send → send via REST (fallback if WebSocket unavailable)
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(chatService.sendMessage(userId, request));
    }

    // GET /api/chat/conversations → get all my conversations
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(chatService.getMyConversations(userId));
    }

    // GET /api/chat/conversations/{conversationId}/messages
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                chatService.getMessages(conversationId, userId, page, size)
        );
    }

    // DELETE /api/chat/messages/{messageId}
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable String messageId) {
        String userId = SecurityUtils.getCurrentUserId();
        chatService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(Map.of("message", "Message deleted"));
    }
}
