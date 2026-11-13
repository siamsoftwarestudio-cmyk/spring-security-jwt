package com.example.security.jwt.config;

import com.example.security.jwt.entity.Role;
import com.example.security.jwt.entity.User;
import com.example.security.jwt.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
            }
            if (!userRepository.existsByUsername("user")) {
                userRepository.save(new User("user", passwordEncoder.encode("user123"), Role.USER));
            }
        };
    }
}
