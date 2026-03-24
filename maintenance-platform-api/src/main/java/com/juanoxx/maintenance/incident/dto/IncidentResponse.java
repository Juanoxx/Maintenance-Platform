package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.IncidentCategory;
import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import com.juanoxx.maintenance.incident.entity.IncidentStatus;

import java.time.OffsetDateTime;

public record IncidentResponse(
        Long id,
        String code,
        String title,
        String description,
        IncidentCategory category,
        IncidentPriority priority,
        IncidentStatus status,
        boolean overdue,
        Long residentId,
        Long technicianId,
        Long buildingId,
        OffsetDateTime createdAt,
        OffsetDateTime assignedAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime closedAt
) {
}
