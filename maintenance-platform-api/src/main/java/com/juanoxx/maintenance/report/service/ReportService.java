package com.juanoxx.maintenance.report.service;

import com.juanoxx.maintenance.incident.dto.IncidentSearchCriteria;
import com.juanoxx.maintenance.incident.entity.Incident;
import com.juanoxx.maintenance.incident.repository.IncidentRepository;
import com.juanoxx.maintenance.incident.service.IncidentSpecifications;
import com.juanoxx.maintenance.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public byte[] exportIncidentsCsv(IncidentSearchCriteria criteria) {
        validateDateRange(criteria);
        List<Incident> incidents = incidentRepository.findAll(
                IncidentSpecifications.byCriteria(criteria),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        StringBuilder csv = new StringBuilder();
        csv.append("id,code,title,category,priority,status,building_id,resident_id,technician_id,created_at,closed_at\n");
        for (Incident incident : incidents) {
            csv.append(incident.getId()).append(',')
                    .append(safe(incident.getCode())).append(',')
                    .append(safe(incident.getTitle())).append(',')
                    .append(incident.getCategory()).append(',')
                    .append(incident.getPriority()).append(',')
                    .append(incident.getStatus()).append(',')
                    .append(incident.getBuilding().getId()).append(',')
                    .append(incident.getResident().getId()).append(',')
                    .append(incident.getTechnician() != null ? incident.getTechnician().getId() : "").append(',')
                    .append(incident.getCreatedAt()).append(',')
                    .append(incident.getClosedAt() != null ? incident.getClosedAt() : "")
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void validateDateRange(IncidentSearchCriteria criteria) {
        if (criteria == null || criteria.createdFrom() == null || criteria.createdTo() == null) {
            return;
        }
        if (criteria.createdFrom().isAfter(criteria.createdTo())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "createdFrom cannot be after createdTo");
        }
    }
}
