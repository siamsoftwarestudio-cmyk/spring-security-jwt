package com.example.security.jwt.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String username,
        String role
) {
}
