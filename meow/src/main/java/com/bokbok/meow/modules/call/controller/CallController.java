package com.bokbok.meow.modules.call.controller;

import com.bokbok.meow.modules.call.dto.CallLogResponse;
import com.bokbok.meow.modules.call.service.CallService;
import com.bokbok.meow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    // GET /api/calls/history → get all call logs
    @GetMapping("/history")
    public ResponseEntity<List<CallLogResponse>> getCallHistory() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(callService.getCallHistory(userId));
    }
}