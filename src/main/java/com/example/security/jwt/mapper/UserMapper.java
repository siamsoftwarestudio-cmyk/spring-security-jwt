package com.example.security.jwt.mapper;

import com.example.security.jwt.dto.RegisterRequest;
import com.example.security.jwt.entity.Role;
import com.example.security.jwt.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = Role.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", source = "request.username")
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "role", expression = "java(Role.USER)")
    User toEntity(RegisterRequest request, String encodedPassword);
}
