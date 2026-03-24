package com.juanoxx.maintenance.incident.repository;

import com.juanoxx.maintenance.incident.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
