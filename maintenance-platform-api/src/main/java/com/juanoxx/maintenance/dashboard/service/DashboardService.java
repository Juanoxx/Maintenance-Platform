package com.juanoxx.maintenance.dashboard.service;

import com.juanoxx.maintenance.common.util.SecurityUtils;
import com.juanoxx.maintenance.dashboard.dto.AdminDashboardResponse;
import com.juanoxx.maintenance.dashboard.dto.ResidentDashboardResponse;
import com.juanoxx.maintenance.dashboard.dto.TechnicianDashboardResponse;
import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import com.juanoxx.maintenance.incident.entity.IncidentStatus;
import com.juanoxx.maintenance.incident.repository.IncidentRepository;
import com.juanoxx.maintenance.user.entity.User;
import com.juanoxx.maintenance.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncidentRepository incidentRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard() {
        return new AdminDashboardResponse(
                incidentRepository.countByStatus(IncidentStatus.PENDIENTE),
                incidentRepository.countByStatus(IncidentStatus.ASIGNADA),
                incidentRepository.countByStatus(IncidentStatus.EN_PROCESO),
                incidentRepository.countByStatus(IncidentStatus.RESUELTA),
                incidentRepository.countByStatus(IncidentStatus.CERRADA),
                incidentRepository.countByPriority(IncidentPriority.CRITICA)
        );
    }

    @Transactional(readOnly = true)
    public TechnicianDashboardResponse technicianDashboard() {
        User technician = userService.getRequiredById(SecurityUtils.currentUser().getId());
        return new TechnicianDashboardResponse(
                incidentRepository.countByTechnicianAndStatus(technician, IncidentStatus.ASIGNADA),
                incidentRepository.countByTechnicianAndStatus(technician, IncidentStatus.EN_PROCESO),
                incidentRepository.countByTechnicianAndStatus(technician, IncidentStatus.RESUELTA)
        );
    }

    @Transactional(readOnly = true)
    public ResidentDashboardResponse residentDashboard() {
        User resident = userService.getRequiredById(SecurityUtils.currentUser().getId());
        long total = incidentRepository.countByResident(resident);
        long cerradas = incidentRepository.findByResidentOrderByCreatedAtDesc(resident)
                .stream()
                .filter(i -> i.getStatus() == IncidentStatus.CERRADA)
                .count();
        return new ResidentDashboardResponse(total, total - cerradas, cerradas);
    }
}
