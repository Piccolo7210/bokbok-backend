package com.bokbok.meow.modules.user.dto;

import com.bokbok.meow.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String avatarUrl;
    private String about;
    private String status;
    private LocalDateTime lastSeen;

    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .about(user.getAbout())
                .status(user.getStatus().name())
                .lastSeen(user.getLastSeen())
                .build();
    }
}