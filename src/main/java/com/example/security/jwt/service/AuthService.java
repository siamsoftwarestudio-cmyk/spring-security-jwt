package com.example.security.jwt.service;

import com.example.security.jwt.dto.AuthResponse;
import com.example.security.jwt.dto.LoginRequest;
import com.example.security.jwt.dto.RefreshRequest;
import com.example.security.jwt.dto.RegisterRequest;
import com.example.security.jwt.entity.RefreshToken;
import com.example.security.jwt.entity.User;
import com.example.security.jwt.exception.ApiException;
import com.example.security.jwt.mapper.AuthMapper;
import com.example.security.jwt.mapper.UserMapper;
import com.example.security.jwt.repository.RefreshTokenRepository;
import com.example.security.jwt.repository.UserRepository;
import com.example.security.jwt.security.JwtService;
import com.example.security.jwt.security.UserPrincipal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException("Username already taken", HttpStatus.CONFLICT);
        }

        User user = userMapper.toEntity(request, passwordEncoder.encode(request.password()));
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        refreshTokenRepository.deleteByUser(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String rawToken = request.refreshToken();

        if (!jwtService.isTokenValid(rawToken) || !"refresh".equals(jwtService.extractTokenType(rawToken))) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new ApiException("Refresh token not found or revoked", HttpStatus.UNAUTHORIZED));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new ApiException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);

        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.deleteByToken(request.refreshToken());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        refreshTokenRepository.save(new RefreshToken(
                refreshToken,
                Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()),
                user
        ));

        return authMapper.toResponse(accessToken, refreshToken, user);
    }
}
