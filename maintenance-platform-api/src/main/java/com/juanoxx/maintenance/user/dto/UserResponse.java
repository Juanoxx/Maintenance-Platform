package com.juanoxx.maintenance.user.dto;

import com.juanoxx.maintenance.user.entity.UserRole;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        UserRole role,
        Long buildingId,
        boolean active
) {
}
