package com.bokbok.meow.modules.call.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallSignalRequest {

    private String targetUserId;  // who to send signal to

    private String type;
    // Possible types:
    // "call.initiate"  → start a call
    // "call.accepted"  → receiver accepted
    // "call.declined"  → receiver declined
    // "call.offer"     → SDP offer  (WebRTC)
    // "call.answer"    → SDP answer (WebRTC)
    // "call.ice"       → ICE candidate
    // "call.end"       → hang up
    // "call.busy"      → receiver is on another call

    private String callType;   // VOICE or VIDEO
    private String callId;     // unique call session ID
    private String sdp;        // SDP offer or answer payload
    private String candidate;  // ICE candidate payload
    private String sdpMid;
    private Integer sdpMLineIndex;
}