package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.IncidentStatus;

import java.time.OffsetDateTime;

public record IncidentStatusHistoryResponse(
        Long id,
        IncidentStatus fromStatus,
        IncidentStatus toStatus,
        Long changedById,
        String reason,
        OffsetDateTime createdAt
) {
}
