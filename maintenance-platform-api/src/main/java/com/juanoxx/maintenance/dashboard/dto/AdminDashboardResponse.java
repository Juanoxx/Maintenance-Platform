package com.juanoxx.maintenance.dashboard.dto;

public record AdminDashboardResponse(
        long pendientes,
        long asignadas,
        long enProceso,
        long resueltas,
        long cerradas,
        long criticas
) {
}
