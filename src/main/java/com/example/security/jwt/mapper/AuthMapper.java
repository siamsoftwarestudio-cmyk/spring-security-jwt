package com.example.security.jwt.mapper;

import com.example.security.jwt.dto.AuthResponse;
import com.example.security.jwt.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "role", source = "user.role")
    AuthResponse toResponse(String accessToken, String refreshToken, User user);
}
