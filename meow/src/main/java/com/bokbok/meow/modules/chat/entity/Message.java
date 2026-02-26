package com.bokbok.meow.modules.chat.entity;
import com.bokbok.meow.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    // Text content — null if media message
    @Column(columnDefinition = "TEXT")
    private String content;

    // Media URL from Cloudinary — null if text message
    private String mediaUrl;

    // For voice notes: duration in seconds
    private Integer mediaDuration;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,        // Voice note
        FILE,
        CALL_LOG      // Missed/received call record
    }

    public enum MessageStatus {
        SENT,         // Saved to DB
        DELIVERED,    // Reached recipient device
        READ          // Recipient opened it
    }
}
