package com.example.security.jwt.dto;

import com.example.security.jwt.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String product,
        Integer quantity,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt,
        String ownerUsername
) {
}
