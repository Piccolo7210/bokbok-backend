package com.bokbok.meow.modules.chat.service;

import com.bokbok.meow.modules.chat.dto.ConversationResponse;
import com.bokbok.meow.modules.chat.dto.MessageResponse;
import com.bokbok.meow.modules.chat.dto.SendMessageRequest;
import com.bokbok.meow.modules.chat.entity.Conversation;
import com.bokbok.meow.modules.chat.entity.Message;
import com.bokbok.meow.modules.chat.repository.ConversationRepository;
import com.bokbok.meow.modules.chat.repository.MessageRepository;
import com.bokbok.meow.modules.notification.service.NotificationService;
import com.bokbok.meow.modules.user.entity.User;
import com.bokbok.meow.modules.user.repository.UserRepository;
import com.bokbok.meow.websocket.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final NotificationService notificationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;

    // ── Send a Message ───────────────────────────────────────────

    @Transactional
    public MessageResponse sendMessage(String senderId,
                                       SendMessageRequest request) {

        User sender = findUser(senderId);
        User receiver = findUser(request.getReceiverId());

        // Get or create conversation
        Conversation conversation = conversationRepository
                .findByUsers(senderId, request.getReceiverId())
                .orElseGet(() -> createConversation(sender, receiver));

        // Build message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .type(Message.MessageType.valueOf(request.getType()))
                .content(request.getContent())
                .mediaUrl(request.getMediaUrl())
                .mediaDuration(request.getMediaDuration())
                .status(Message.MessageStatus.SENT)
                .build();

        // If receiver is online → mark as DELIVERED immediately
        if (presenceService.isOnline(receiver.getId())) {
            message.setStatus(Message.MessageStatus.DELIVERED);
            message.setDeliveredAt(LocalDateTime.now());
        }

        messageRepository.save(message);

        // Update conversation preview
        String preview = buildPreview(message);
        conversation.setLastMessagePreview(preview);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);

        MessageResponse response = MessageResponse.fromEntity(
                message, receiver.getId()
        );

        // Push to receiver in real-time via WebSocket
        messagingTemplate.convertAndSendToUser(
                receiver.getId(),
                "/queue/messages",
                response
        );

// If receiver is OFFLINE → send push notification
        if (!presenceService.isOnline(receiver.getId())) {
            notificationService.sendMessageNotification(
                    receiver.getId(),
                    sender.getName(),
                    preview,
                    conversation.getId()
            );
        }

        // Refresh sender presence
        presenceService.refreshPresence(senderId);

        return response;
    }

    // ── Get All Conversations for a User ────────────────────────

    public List<ConversationResponse> getMyConversations(String userId) {
        return conversationRepository.findAllByUserId(userId)
                .stream()
                .map(conv -> buildConversationResponse(conv, userId))
                .collect(Collectors.toList());
    }

    // ── Get Messages in a Conversation ──────────────────────────

    @Transactional
    public List<MessageResponse> getMessages(String conversationId,
                                             String userId,
                                             int page,
                                             int size) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Determine the other user
        String otherUserId = conversation.getUser1().getId().equals(userId)
                ? conversation.getUser2().getId()
                : conversation.getUser1().getId();

        List<Message> messages = messageRepository
                .findByConversationIdPaged(
                        conversationId,
                        PageRequest.of(page, size)
                ).getContent();

        // Mark all unread messages as READ
        messages.stream()
                .filter(m -> !m.getSender().getId().equals(userId)
                        && m.getStatus() != Message.MessageStatus.READ)
                .forEach(m -> {
                    m.setStatus(Message.MessageStatus.READ);
                    m.setReadAt(LocalDateTime.now());
                    messageRepository.save(m);

                    // Notify sender their message was read
                    messagingTemplate.convertAndSendToUser(
                            m.getSender().getId(),
                            "/queue/message-status",
                            new com.bokbok.meow.modules.chat.dto
                                    .MessageStatusUpdate(
                                    m.getId(),
                                    conversationId,
                                    "READ"
                            )
                    );
                });

        return messages.stream()
                .map(m -> MessageResponse.fromEntity(m, otherUserId))
                .collect(Collectors.toList());
    }

    // ── Mark Messages as Delivered ──────────────────────────────

    @Transactional
    public void markDelivered(String userId) {
        List<Message> undelivered = messageRepository
                .findUndeliveredMessages(userId);

        undelivered.forEach(m -> {
            m.setStatus(Message.MessageStatus.DELIVERED);
            m.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(m);

            // Notify sender
            messagingTemplate.convertAndSendToUser(
                    m.getSender().getId(),
                    "/queue/message-status",
                    new com.bokbok.meow.modules.chat.dto
                            .MessageStatusUpdate(
                            m.getId(),
                            m.getConversation().getId(),
                            "DELIVERED"
                    )
            );
        });
    }

    // ── Delete a Message ────────────────────────────────────────

    @Transactional
    public void deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own messages");
        }

        message.setDeleted(true);
        messageRepository.save(message);
    }

    // ── Private Helpers ─────────────────────────────────────────

    private Conversation createConversation(User user1, User user2) {
        // Always sort IDs to avoid duplicates
        User first = user1.getId().compareTo(user2.getId()) < 0 ? user1 : user2;
        User second = first == user1 ? user2 : user1;

        Conversation conv = Conversation.builder()
                .user1(first)
                .user2(second)
                .build();
        return conversationRepository.save(conv);
    }

    private ConversationResponse buildConversationResponse(
            Conversation conv, String userId) {

        User other = conv.getUser1().getId().equals(userId)
                ? conv.getUser2()
                : conv.getUser1();

        return ConversationResponse.builder()
                .id(conv.getId())
                .otherUserId(other.getId())
                .otherUserName(other.getName())
                .otherUserAvatarUrl(other.getAvatarUrl())
                .otherUserStatus(other.getStatus().name())
                .lastMessagePreview(conv.getLastMessagePreview())
                .lastMessageAt(conv.getLastMessageAt())
                .build();
    }

    private String buildPreview(Message message) {
        return switch (message.getType()) {
            case TEXT -> message.getContent().length() > 40
                    ? message.getContent().substring(0, 40) + "..."
                    : message.getContent();
            case IMAGE -> "📷 Photo";
            case VIDEO -> "🎥 Video";
            case AUDIO -> "🎤 Voice note";
            case FILE  -> "📎 File";
            case CALL_LOG -> "📞 Call";
        };
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}