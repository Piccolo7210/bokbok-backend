package com.bokbok.meow.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStatusUpdate {
    private String messageId;
    private String conversationId;
    private String status;  // DELIVERED or READ
}