package com.juanoxx.maintenance.incident.dto;

import jakarta.validation.constraints.NotNull;

public record IncidentAssignRequest(
        @NotNull Long technicianId
) {
}
