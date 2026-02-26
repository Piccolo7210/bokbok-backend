package com.bokbok.meow.modules.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.bokbok.meow.modules.user.entity.User;
import com.bokbok.meow.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserRepository userRepository;

    // ── New Chat Message Notification ────────────────────────────

    public void sendMessageNotification(String toUserId,
                                        String fromUserName,
                                        String messagePreview,
                                        String conversationId) {
        User user = getUser(toUserId);
        if (user == null || user.getFcmToken() == null) return;

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(fromUserName)
                        .setBody(messagePreview)
                        .build()
                )
                // Extra data Flutter reads to navigate to the right screen
                .putAllData(Map.of(
                        "type",           "NEW_MESSAGE",
                        "conversationId", conversationId,
                        "senderName",     fromUserName
                ))
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                .build()
                        )
                        .build()
                )
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setAlert(ApsAlert.builder()
                                        .setTitle(fromUserName)
                                        .setBody(messagePreview)
                                        .build()
                                )
                                .setSound("default")
                                .setBadge(1)
                                .build()
                        )
                        .build()
                )
                .build();

        send(message, toUserId);
    }

    // ── Incoming Call Notification ───────────────────────────────

    public void sendCallNotification(String toUserId,
                                     String fromUserName,
                                     String fromAvatarUrl,
                                     String callType,
                                     String callId) {
        User user = getUser(toUserId);
        if (user == null || user.getFcmToken() == null) return;

        String title = callType.equals("VIDEO")
                ? "📹 Incoming Video Call"
                : "📞 Incoming Voice Call";

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(fromUserName + " is calling you")
                        .build()
                )
                .putAllData(Map.of(
                        "type",         "INCOMING_CALL",
                        "callId",       callId,
                        "callType",     callType,
                        "callerName",   fromUserName,
                        "callerAvatar", fromAvatarUrl != null
                                ? fromAvatarUrl : ""
                ))
                // High priority — wakes up device screen for calls
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                .build()
                        )
                        .build()
                )
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setAlert(ApsAlert.builder()
                                        .setTitle(title)
                                        .setBody(fromUserName + " is calling you")
                                        .build()
                                )
                                .setSound("default")
                                .setBadge(1)
                                .build()
                        )
                        .build()
                )
                .build();

        send(message, toUserId);
    }

    // ── Missed Call Notification ─────────────────────────────────

    public void sendMissedCallNotification(String toUserId,
                                           String fromUserName,
                                           String callType) {
        User user = getUser(toUserId);
        if (user == null || user.getFcmToken() == null) return;

        String body = callType.equals("VIDEO")
                ? "Missed video call from " + fromUserName
                : "Missed voice call from " + fromUserName;

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle("Missed Call")
                        .setBody(body)
                        .build()
                )
                .putAllData(Map.of(
                        "type",       "MISSED_CALL",
                        "callerName", fromUserName,
                        "callType",   callType
                ))
                .build();

        send(message, toUserId);
    }

    // ── Private Helpers ──────────────────────────────────────────

    private void send(Message message, String toUserId) {
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent to user {}: {}", toUserId, response);
        } catch (Exception e) {
            log.error("FCM failed for user {}: {}", toUserId, e.getMessage());
        }
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
}