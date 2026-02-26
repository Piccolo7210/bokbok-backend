package com.bokbok.meow.modules.user.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateFcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}
