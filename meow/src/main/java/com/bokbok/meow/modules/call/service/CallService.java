package com.bokbok.meow.modules.call.service;

import com.bokbok.meow.modules.call.dto.CallLogResponse;
import com.bokbok.meow.modules.call.dto.CallSignalRequest;
import com.bokbok.meow.modules.call.dto.CallSignalResponse;
import com.bokbok.meow.modules.call.entity.CallLog;
import com.bokbok.meow.modules.call.repository.CallLogRepository;
import com.bokbok.meow.modules.chat.entity.Message;
import com.bokbok.meow.modules.chat.entity.Message.MessageType;
import com.bokbok.meow.modules.chat.service.ChatService;
import com.bokbok.meow.modules.notification.service.NotificationService;
import com.bokbok.meow.modules.user.entity.User;
import com.bokbok.meow.modules.user.repository.UserRepository;
import com.bokbok.meow.websocket.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallService {
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CallLogRepository callLogRepository;
    private final UserRepository userRepository;
    private final ActiveCallStore activeCallStore;
    private final PresenceService presenceService;

    @Transactional
    public void handleSignal(String fromUserId, CallSignalRequest request) {

        User fromUser = findUser(fromUserId);
        User targetUser = findUser(request.getTargetUserId());

        switch (request.getType()) {

            case "call.initiate" -> handleInitiate(
                    fromUser, targetUser, request
            );

            case "call.accepted" -> handleAccepted(
                    fromUser, targetUser, request
            );

            case "call.declined" -> handleDeclined(
                    fromUser, targetUser, request
            );

            case "call.offer",
                 "call.answer",
                 "call.ice"     -> relaySignal(
                    fromUser, targetUser, request
            );

            case "call.end"     -> handleEnd(
                    fromUser, targetUser, request
            );

            default -> log.warn(
                    "Unknown call signal type: {}", request.getType()
            );
        }
    }

    // ── Initiate Call ────────────────────────────────────────────

    private void handleInitiate(User caller, User receiver,
                                CallSignalRequest request) {

        // Check if receiver is on another call
        if (activeCallStore.isOnCall(receiver.getId())) {
            sendSignal(receiver.getId(), caller.getId(),
                    CallSignalResponse.builder()
                            .type("call.busy")
                            .callId(request.getCallId())
                            .callType(request.getCallType())
                            .fromUserId(receiver.getId())
                            .fromUserName(receiver.getName())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
            return;
        }

        String callId = request.getCallId() != null
                ? request.getCallId()
                : UUID.randomUUID().toString();

        // Mark both users as in a call
        activeCallStore.startCall(caller.getId(), callId);
        activeCallStore.startCall(receiver.getId(), callId);

        // Forward initiation signal to receiver
        sendSignal(receiver.getId(), caller.getId(),
                CallSignalResponse.builder()
                        .type("call.initiate")
                        .callId(callId)
                        .callType(request.getCallType())
                        .fromUserId(caller.getId())
                        .fromUserName(caller.getName())
                        .fromAvatarUrl(caller.getAvatarUrl())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
        if (!presenceService.isOnline(receiver.getId())) {
            notificationService.sendCallNotification(
                    receiver.getId(),
                    caller.getName(),
                    caller.getAvatarUrl(),
                    request.getCallType(),
                    callId
            );
        }

        log.info("Call initiated: {} → {} [{}]",
                caller.getName(), receiver.getName(), request.getCallType());
    }

    // ── Accept Call ──────────────────────────────────────────────

    private void handleAccepted(User receiver, User caller,
                                CallSignalRequest request) {
        activeCallStore.refreshCall(caller.getId());
        activeCallStore.refreshCall(receiver.getId());

        sendSignal(caller.getId(), receiver.getId(),
                CallSignalResponse.builder()
                        .type("call.accepted")
                        .callId(request.getCallId())
                        .callType(request.getCallType())
                        .fromUserId(receiver.getId())
                        .fromUserName(receiver.getName())
                        .fromAvatarUrl(receiver.getAvatarUrl())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ── Decline Call ─────────────────────────────────────────────

    @Transactional
    protected void handleDeclined(User receiver, User caller,
                                CallSignalRequest request) {

        activeCallStore.endCall(caller.getId());
        activeCallStore.endCall(receiver.getId());

        // Save missed call log
        saveCallLog(
                caller, receiver,
                request.getCallType(),
                CallLog.CallStatus.MISSED,
                null, null
        );

        sendSignal(caller.getId(), receiver.getId(),
                CallSignalResponse.builder()
                        .type("call.declined")
                        .callId(request.getCallId())
                        .callType(request.getCallType())
                        .fromUserId(receiver.getId())
                        .fromUserName(receiver.getName())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
        notificationService.sendMissedCallNotification(
                caller.getId(),
                receiver.getName(),
                request.getCallType()
        );
    }

    // ── Relay SDP / ICE ──────────────────────────────────────────

    private void relaySignal(User from, User target,
                             CallSignalRequest request) {

        activeCallStore.refreshCall(from.getId());

        sendSignal(target.getId(), from.getId(),
                CallSignalResponse.builder()
                        .type(request.getType())
                        .callId(request.getCallId())
                        .callType(request.getCallType())
                        .fromUserId(from.getId())
                        .sdp(request.getSdp())
                        .candidate(request.getCandidate())
                        .sdpMid(request.getSdpMid())
                        .sdpMLineIndex(request.getSdpMLineIndex())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ── End Call ─────────────────────────────────────────────────

    @Transactional
    protected void handleEnd(User from, User target,
                           CallSignalRequest request) {

        activeCallStore.endCall(from.getId());
        activeCallStore.endCall(target.getId());

        // Save answered call log
        saveCallLog(
                from, target,
                request.getCallType(),
                CallLog.CallStatus.ENDED,
                LocalDateTime.now().minusSeconds(60), // approximate
                LocalDateTime.now()
        );

        sendSignal(target.getId(), from.getId(),
                CallSignalResponse.builder()
                        .type("call.end")
                        .callId(request.getCallId())
                        .callType(request.getCallType())
                        .fromUserId(from.getId())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // ── Get Call History ─────────────────────────────────────────

    public List<CallLogResponse> getCallHistory(String userId) {
        return callLogRepository
                .findByCallerIdOrReceiverIdOrderByCreatedAtDesc(
                        userId, userId
                )
                .stream()
                .map(CallLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ──────────────────────────────────────────

    private void sendSignal(String toUserId, String fromUserId,
                            CallSignalResponse signal) {
        messagingTemplate.convertAndSendToUser(
                toUserId,
                "/queue/call",
                signal
        );
    }

    private void saveCallLog(User caller, User receiver,
                             String callType,
                             CallLog.CallStatus status,
                             LocalDateTime startedAt,
                             LocalDateTime endedAt) {

        Integer duration = null;
        if (startedAt != null && endedAt != null) {
            duration = (int) java.time.Duration.between(
                    startedAt, endedAt
            ).getSeconds();
        }

        CallLog log = CallLog.builder()
                .caller(caller)
                .receiver(receiver)
                .type(CallLog.CallType.valueOf(
                        callType != null ? callType : "VOICE"
                ))
                .status(status)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .duration(duration)
                .build();

        callLogRepository.save(log);
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId)
                );
    }
}