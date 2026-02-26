package com.bokbok.meow.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingEvent {
    private String senderId;
    private String receiverId;
    private boolean typing;  // true = started typing, false = stopped
}