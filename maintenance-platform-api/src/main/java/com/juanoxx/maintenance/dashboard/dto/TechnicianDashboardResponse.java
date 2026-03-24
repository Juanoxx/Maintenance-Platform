package com.juanoxx.maintenance.dashboard.dto;

public record TechnicianDashboardResponse(
        long asignadas,
        long enProceso,
        long resueltas
) {
}
