package com.bokbok.meow.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private String id;
    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private String otherUserStatus;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
}