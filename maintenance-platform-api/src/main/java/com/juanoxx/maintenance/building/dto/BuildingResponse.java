package com.juanoxx.maintenance.building.dto;

public record BuildingResponse(
        Long id,
        String name,
        String address,
        String commune,
        Long adminUserId,
        boolean active
) {
}
