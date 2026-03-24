package com.juanoxx.maintenance.incident.scheduler;

import com.juanoxx.maintenance.incident.entity.Incident;
import com.juanoxx.maintenance.incident.entity.IncidentStatus;
import com.juanoxx.maintenance.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentOverdueScheduler {

    private final IncidentRepository incidentRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void markPendingOverdueIncidents() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(3);
        List<Incident> incidents = incidentRepository.findByStatusAndOverdueFalseAndCreatedAtBefore(
                IncidentStatus.PENDIENTE,
                cutoff
        );
        incidents.forEach(incident -> incident.setOverdue(true));
        if (!incidents.isEmpty()) {
            incidentRepository.saveAll(incidents);
            log.info("Marked {} incidents as overdue", incidents.size());
        }
    }
}
