package com.bokbok.meow.modules.user.controller;

import com.bokbok.meow.modules.user.dto.UpdateFcmTokenRequest;
import com.bokbok.meow.modules.user.dto.UpdateProfileRequest;
import com.bokbok.meow.modules.user.dto.UserProfileResponse;
import com.bokbok.meow.modules.user.service.UserService;
import com.bokbok.meow.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me → my profile
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.getMyProfile(userId));
    }

    // GET /api/users/{userId} → any user's profile
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(
            @PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // GET /api/users/phone/{phone} → find user by phone number
    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserProfileResponse> getUserByPhone(
            @PathVariable String phone) {
        return ResponseEntity.ok(userService.getUserByPhone(phone));
    }

    // GET /api/users/search?query=john → search users by name
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsers(
            @RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    // PUT /api/users/me → update name, about, email
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    // PUT /api/users/me/avatar → upload profile picture
    @PutMapping(value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateAvatar(
            HttpServletRequest request,
            @RequestParam Map<String, MultipartFile> files) {

        // Try both "file" and "File"
        MultipartFile file = files.containsKey("file")
                ? files.get("file")
                : files.values().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No file provided"));

        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.updateAvatar(userId, file));
    }
    @PutMapping("/me/fcm-token")
    public ResponseEntity<Map<String, String>> updateFcmToken(
            @Valid @RequestBody UpdateFcmTokenRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        userService.updateFcmToken(userId, request.getFcmToken());
        return ResponseEntity.ok(Map.of("message", "FCM token updated"));
    }
}