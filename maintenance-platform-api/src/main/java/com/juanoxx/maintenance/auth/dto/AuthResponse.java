package com.juanoxx.maintenance.auth.dto;

import com.juanoxx.maintenance.user.entity.UserRole;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserSummary user
) {
    public record UserSummary(
            Long id,
            String fullName,
            String email,
            UserRole role,
            Long buildingId
    ) {
    }
}
