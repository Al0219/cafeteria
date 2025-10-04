package com.app.cafeteria.dtos.auth;

import java.time.OffsetDateTime;
import java.util.List;

public record LoginResponse(
        Integer userId,
        String username,
        String email,
        String nombre,
        List<String> roles,
        String token,
        OffsetDateTime expiresAt
) {}
