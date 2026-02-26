package com.bokbok.meow.modules.call.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class IceConfigController {

    // Flutter fetches this on app start to configure WebRTC
    @GetMapping("/ice-config")
    public ResponseEntity<Map<String, Object>> getIceConfig() {
        return ResponseEntity.ok(Map.of(
                "iceServers", List.of(
                        // Free Google STUN servers
                        Map.of("urls", List.of(
                                "stun:stun.l.google.com:19302",
                                "stun:stun1.l.google.com:19302",
                                "stun:stun2.l.google.com:19302"
                        )),
                        // Free Metered TURN server
                        // Sign up free at https://www.metered.ca/stun-turn
                        Map.of(
                                "urls",       "turn:a.relay.metered.ca:80",
                                "username",   System.getenv("TURN_USERNAME"),
                                "credential", System.getenv("TURN_CREDENTIAL")
                        ),
                        Map.of(
                                "urls",       "turn:a.relay.metered.ca:443",
                                "username",   System.getenv("TURN_USERNAME"),
                                "credential", System.getenv("TURN_CREDENTIAL")
                        )
                )
        ));
    }
}