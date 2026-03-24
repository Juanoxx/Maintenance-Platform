package com.juanoxx.maintenance.building.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BuildingRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 200) String address,
        @NotBlank @Size(max = 80) String commune
) {
}
