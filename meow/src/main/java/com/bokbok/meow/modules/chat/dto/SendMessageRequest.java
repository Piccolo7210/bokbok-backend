package com.bokbok.meow.modules.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Receiver ID is required")
    private String receiverId;

    private String content;         // null if media message

    @NotBlank(message = "Message type is required")
    private String type;            // TEXT, IMAGE, VIDEO, AUDIO, FILE

    private String mediaUrl;        // Cloudinary URL — set after upload
    private Integer mediaDuration;  // For voice notes (seconds)
}