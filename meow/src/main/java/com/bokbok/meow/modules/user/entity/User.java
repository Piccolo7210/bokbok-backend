package com.bokbok.meow.modules.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String avatarUrl;

    private String about;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    private LocalDateTime lastSeen;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean isVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "fcm_token")
    private String fcmToken;

    public enum UserStatus {
        ONLINE, OFFLINE, AWAY
    }
}