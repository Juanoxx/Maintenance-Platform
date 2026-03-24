package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import jakarta.validation.constraints.NotNull;

public record IncidentPriorityUpdateRequest(
        @NotNull IncidentPriority priority
) {
}
