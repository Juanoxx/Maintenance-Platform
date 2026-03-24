package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentStatusUpdateRequest(
        @NotNull IncidentStatus status,
        @Size(max = 500) String reason,
        @Size(max = 2500) String comment
) {
}
