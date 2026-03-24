package com.juanoxx.maintenance.dashboard.controller;

import com.juanoxx.maintenance.dashboard.dto.AdminDashboardResponse;
import com.juanoxx.maintenance.dashboard.dto.ResidentDashboardResponse;
import com.juanoxx.maintenance.dashboard.dto.TechnicianDashboardResponse;
import com.juanoxx.maintenance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse admin() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/technician")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public TechnicianDashboardResponse technician() {
        return dashboardService.technicianDashboard();
    }

    @GetMapping("/resident")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResidentDashboardResponse resident() {
        return dashboardService.residentDashboard();
    }
}
