package com.bokbok.meow.modules.auth.service;

import com.bokbok.meow.modules.auth.dto.AuthResponse;
import com.bokbok.meow.modules.auth.dto.LoginRequest;
import com.bokbok.meow.modules.auth.dto.RefreshTokenRequest;
import com.bokbok.meow.modules.auth.dto.RegisterRequest;
import com.bokbok.meow.modules.auth.entity.RefreshToken;
import com.bokbok.meow.modules.auth.repository.RefreshTokenRepository;
import com.bokbok.meow.modules.user.entity.User;
import com.bokbok.meow.modules.user.repository.UserRepository;
import com.bokbok.meow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isVerified(true)
                .status(User.UserStatus.OFFLINE)
                .build();

        userRepository.save(user);

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BadCredentialsException("Invalid phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid phone or password");
        }

        // Update status to online
        user.setStatus(User.UserStatus.ONLINE);
        userRepository.save(user);

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Rotate refresh token — revoke old, issue new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateAuthResponse(storedToken.getUser());
    }

    @Transactional
    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Revoke all refresh tokens
        refreshTokenRepository.deleteAllByUser(user);

        // Set status offline
        user.setStatus(User.UserStatus.OFFLINE);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    // ── Private Helper ──────────────────────────────────────────

    private AuthResponse generateAuthResponse(User user) {

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiry / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}