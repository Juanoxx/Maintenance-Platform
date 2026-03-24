package com.juanoxx.maintenance.report.controller;

import com.juanoxx.maintenance.incident.dto.IncidentSearchCriteria;
import com.juanoxx.maintenance.incident.entity.IncidentCategory;
import com.juanoxx.maintenance.incident.entity.IncidentPriority;
import com.juanoxx.maintenance.incident.entity.IncidentStatus;
import com.juanoxx.maintenance.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/incidents.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportIncidentsCsv(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentPriority priority,
            @RequestParam(required = false) IncidentCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Boolean overdue
    ) {
        IncidentSearchCriteria criteria = new IncidentSearchCriteria(
                buildingId,
                status,
                priority,
                category,
                createdFrom,
                createdTo,
                technicianId,
                overdue
        );
        byte[] body = reportService.exportIncidentsCsv(criteria);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("incidents.csv").build().toString())
                .body(body);
    }
}
