package com.juanoxx.maintenance.incident.repository;

import com.juanoxx.maintenance.incident.entity.IncidentStatus;
import com.juanoxx.maintenance.incident.entity.IncidentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentStatusHistoryRepository extends JpaRepository<IncidentStatusHistory, Long> {

    List<IncidentStatusHistory> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);

    boolean existsByIncidentIdAndToStatus(Long incidentId, IncidentStatus toStatus);
}
