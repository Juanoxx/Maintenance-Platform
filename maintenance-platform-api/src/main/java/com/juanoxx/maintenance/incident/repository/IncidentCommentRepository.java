package com.juanoxx.maintenance.incident.repository;

import com.juanoxx.maintenance.incident.entity.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {

    List<IncidentComment> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
