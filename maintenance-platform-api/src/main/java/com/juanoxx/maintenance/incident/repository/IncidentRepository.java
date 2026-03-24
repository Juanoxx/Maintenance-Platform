package com.juanoxx.maintenance.incident.repository;

import com.juanoxx.maintenance.incident.entity.Incident;
import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import com.juanoxx.maintenance.incident.entity.IncidentStatus;
import com.juanoxx.maintenance.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    List<Incident> findByResidentOrderByCreatedAtDesc(User resident);

    List<Incident> findByTechnicianOrderByCreatedAtDesc(User technician);

    long countByStatus(IncidentStatus status);

    long countByPriority(IncidentPriority priority);

    long countByResident(User resident);

    long countByTechnicianAndStatus(User technician, IncidentStatus status);

    List<Incident> findByStatusAndOverdueFalseAndCreatedAtBefore(IncidentStatus status, OffsetDateTime cutoff);
}
