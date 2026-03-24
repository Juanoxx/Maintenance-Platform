package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.IncidentCategory;
import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentCreateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2500) String description,
        @NotNull IncidentCategory category,
        @NotNull IncidentPriority priority,
        @NotNull Long buildingId
) {
}
