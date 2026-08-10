package com.taskflow.api.dto.response;

import java.util.Set;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String email,
        Set<String> roles
) {
    public static AuthResponse of(String token, Long userId, String email, Set<String> roles) {
        return new AuthResponse(token, "Bearer", userId, email, roles);
    }
}
