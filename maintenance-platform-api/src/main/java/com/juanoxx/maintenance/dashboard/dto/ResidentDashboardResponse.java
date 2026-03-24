package com.juanoxx.maintenance.dashboard.dto;

public record ResidentDashboardResponse(
        long totalIncidents,
        long abiertas,
        long cerradas
) {
}
