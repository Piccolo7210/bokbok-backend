package com.bokbok.meow.modules.user.service;

import com.bokbok.meow.modules.media.service.MediaService;
import com.bokbok.meow.modules.user.dto.UpdateProfileRequest;
import com.bokbok.meow.modules.user.dto.UserProfileResponse;
import com.bokbok.meow.modules.user.entity.User;
import com.bokbok.meow.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;

    // Get my own profile
    public UserProfileResponse getMyProfile(String userId) {
        User user = findUserById(userId);
        return UserProfileResponse.fromEntity(user);
    }

    // Get any user's profile by ID
    public UserProfileResponse getUserById(String userId) {
        User user = findUserById(userId);
        return UserProfileResponse.fromEntity(user);
    }

    // Get user profile by phone number
    public UserProfileResponse getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserProfileResponse.fromEntity(user);
    }

    // Search users by name
    public List<UserProfileResponse> searchUsers(String query) {
        return userRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // Update name, about, email
    @Transactional
    public UserProfileResponse updateProfile(String userId,
                                             UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getAbout() != null) {
            user.setAbout(request.getAbout());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        return UserProfileResponse.fromEntity(user);
    }

    // Upload & update avatar
    @Transactional
    public UserProfileResponse updateAvatar(String userId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No file provided");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed for avatar");
        }

        User user = findUserById(userId);
        String avatarUrl = mediaService.uploadAvatar(file, userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return UserProfileResponse.fromEntity(user);
    }
    @Transactional
    public void updateFcmToken(String userId, String fcmToken) {
        User user = findUserById(userId);
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    // ── Private Helper ───────────────────────────────────────────

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}