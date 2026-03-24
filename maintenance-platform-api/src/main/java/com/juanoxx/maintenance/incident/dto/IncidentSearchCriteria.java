package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.IncidentCategory;
import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import com.juanoxx.maintenance.incident.entity.IncidentStatus;

import java.time.LocalDate;

public record IncidentSearchCriteria(
        Long buildingId,
        IncidentStatus status,
        IncidentPriority priority,
        IncidentCategory category,
        LocalDate createdFrom,
        LocalDate createdTo,
        Long technicianId,
        Boolean overdue
) {
}
