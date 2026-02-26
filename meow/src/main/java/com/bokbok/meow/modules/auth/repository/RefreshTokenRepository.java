package com.bokbok.meow.modules.auth.repository;

import com.bokbok.meow.modules.auth.entity.RefreshToken;
import com.bokbok.meow.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(User user);
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
}