package com.bokbok.meow.modules.call.dto;

import com.bokbok.meow.modules.call.entity.CallLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallLogResponse {
    private String id;
    private String callerId;
    private String callerName;
    private String callerAvatarUrl;
    private String receiverId;
    private String receiverName;
    private String receiverAvatarUrl;
    private String type;
    private String status;
    private Integer duration;
    private LocalDateTime createdAt;

    public static CallLogResponse fromEntity(CallLog log) {
        return CallLogResponse.builder()
                .id(log.getId())
                .callerId(log.getCaller().getId())
                .callerName(log.getCaller().getName())
                .callerAvatarUrl(log.getCaller().getAvatarUrl())
                .receiverId(log.getReceiver().getId())
                .receiverName(log.getReceiver().getName())
                .receiverAvatarUrl(log.getReceiver().getAvatarUrl())
                .type(log.getType().name())
                .status(log.getStatus().name())
                .duration(log.getDuration())
                .createdAt(log.getCreatedAt())
                .build();
    }
}