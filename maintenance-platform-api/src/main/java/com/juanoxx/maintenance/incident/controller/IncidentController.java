package com.juanoxx.maintenance.incident.controller;

import com.juanoxx.maintenance.incident.dto.*;
import com.juanoxx.maintenance.incident.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','TECHNICIAN')")
    public List<IncidentResponse> listIncidents() {
        return incidentService.listIncidents();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','TECHNICIAN')")
    public IncidentResponse getIncident(@PathVariable Long id) {
        return incidentService.getIncident(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESIDENT')")
    public IncidentResponse createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        return incidentService.createIncident(request);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public IncidentResponse assignIncident(@PathVariable Long id, @Valid @RequestBody IncidentAssignRequest request) {
        return incidentService.assignTechnician(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','TECHNICIAN')")
    public IncidentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody IncidentStatusUpdateRequest request) {
        return incidentService.updateStatus(id, request);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','TECHNICIAN')")
    public IncidentCommentResponse addComment(@PathVariable Long id, @Valid @RequestBody IncidentCommentRequest request) {
        return incidentService.addComment(id, request);
    }

    @PostMapping("/{id}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENT','TECHNICIAN')")
    public AttachmentResponse uploadAttachment(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return incidentService.uploadAttachment(id, file);
    }
}
