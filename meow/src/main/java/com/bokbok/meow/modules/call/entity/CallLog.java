package com.bokbok.meow.modules.call.entity;
import com.bokbok.meow.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "call_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    private CallType type;       // VOICE or VIDEO

    @Enumerated(EnumType.STRING)
    private CallStatus status;   // MISSED, ANSWERED, DECLINED, ENDED

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    // Duration in seconds
    private Integer duration;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum CallType {
        VOICE, VIDEO
    }

    public enum CallStatus {
        MISSED, ANSWERED, DECLINED, ENDED
    }
}
