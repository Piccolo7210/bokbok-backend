package com.bokbok.meow.modules.call.controller;

import com.bokbok.meow.modules.call.dto.CallSignalRequest;
import com.bokbok.meow.modules.call.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallWebSocketController {

    private final CallService callService;

    // All call signals go through one endpoint: /app/call.signal
    // The type field inside the payload determines what happens
    @MessageMapping("/call.signal")
    public void handleCallSignal(@Payload CallSignalRequest request,
                                 Principal principal) {
        // Guard against unauthenticated sessions
        if (principal == null) {
            log.warn("Call signal rejected: unauthenticated WebSocket session");
            return;
        }
        String fromUserId = principal.getName();
        callService.handleSignal(fromUserId, request);
    }
}