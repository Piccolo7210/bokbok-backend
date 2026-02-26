package com.bokbok.meow.modules.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallSignalResponse {
    private String type;
    private String callId;
    private String callType;      // VOICE or VIDEO
    private String fromUserId;
    private String fromUserName;
    private String fromAvatarUrl;
    private String sdp;
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
    private LocalDateTime timestamp;
}