package com.bokbok.meow.modules.chat.entity;
import com.bokbok.meow.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Always store user1_id < user2_id alphabetically
    // to prevent duplicate conversations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<Message> messages = new ArrayList<>();

    // Last message preview for chat list
    private String lastMessagePreview;

    private LocalDateTime lastMessageAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
