package com.example.security.jwt.repository;

import com.example.security.jwt.entity.RefreshToken;
import com.example.security.jwt.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);
}
