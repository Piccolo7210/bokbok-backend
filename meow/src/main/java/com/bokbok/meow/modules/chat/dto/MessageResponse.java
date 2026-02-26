package com.bokbok.meow.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.bokbok.meow.modules.chat.entity.Message;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String receiverId;
    private String type;
    private String content;
    private String mediaUrl;
    private Integer mediaDuration;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    public static MessageResponse fromEntity(Message message, String receiverId) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .receiverId(receiverId)
                .type(message.getType().name())
                .content(message.getContent())
                .mediaUrl(message.getMediaUrl())
                .mediaDuration(message.getMediaDuration())
                .status(message.getStatus().name())
                .createdAt(message.getCreatedAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .build();
    }
}
