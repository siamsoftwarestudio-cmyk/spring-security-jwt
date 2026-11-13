package com.example.security.jwt.dto;

import com.example.security.jwt.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull OrderStatus status
) {
}
