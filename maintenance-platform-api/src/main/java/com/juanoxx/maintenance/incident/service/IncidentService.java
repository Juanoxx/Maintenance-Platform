package com.juanoxx.maintenance.incident.service;

import com.juanoxx.maintenance.building.entity.Building;
import com.juanoxx.maintenance.building.repository.BuildingRepository;
import com.juanoxx.maintenance.common.exception.BusinessException;
import com.juanoxx.maintenance.common.exception.ForbiddenOperationException;
import com.juanoxx.maintenance.common.exception.ResourceNotFoundException;
import com.juanoxx.maintenance.common.util.SecurityUtils;
import com.juanoxx.maintenance.incident.dto.*;
import com.juanoxx.maintenance.incident.entity.*;
import com.juanoxx.maintenance.incident.repository.AttachmentRepository;
import com.juanoxx.maintenance.incident.repository.IncidentCommentRepository;
import com.juanoxx.maintenance.incident.repository.IncidentRepository;
import com.juanoxx.maintenance.incident.repository.IncidentStatusHistoryRepository;
import com.juanoxx.maintenance.security.model.AuthenticatedUser;
import com.juanoxx.maintenance.storage.service.LocalStorageService;
import com.juanoxx.maintenance.user.entity.User;
import com.juanoxx.maintenance.user.entity.UserRole;
import com.juanoxx.maintenance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final IncidentCommentRepository incidentCommentRepository;
    private final AttachmentRepository attachmentRepository;
    private final IncidentStatusHistoryRepository statusHistoryRepository;
    private final LocalStorageService localStorageService;

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED_TRANSITIONS = Map.of(
            IncidentStatus.PENDIENTE, Set.of(IncidentStatus.ASIGNADA, IncidentStatus.CANCELADA),
            IncidentStatus.ASIGNADA, Set.of(IncidentStatus.EN_PROCESO, IncidentStatus.PENDIENTE),
            IncidentStatus.EN_PROCESO, Set.of(IncidentStatus.RESUELTA, IncidentStatus.ASIGNADA),
            IncidentStatus.RESUELTA, Set.of(IncidentStatus.CERRADA, IncidentStatus.REABIERTA),
            IncidentStatus.REABIERTA, Set.of(IncidentStatus.ASIGNADA, IncidentStatus.EN_PROCESO),
            IncidentStatus.CERRADA, Set.of(),
            IncidentStatus.CANCELADA, Set.of()
    );

    @Transactional(readOnly = true)
    public List<IncidentResponse> listIncidents() {
        AuthenticatedUser principal = SecurityUtils.currentUser();
        List<Incident> incidents;
        if (principal.getRole() == UserRole.ADMIN) {
            incidents = incidentRepository.findAll();
        } else if (principal.getRole() == UserRole.TECHNICIAN) {
            User technician = getRequiredUser(principal.getId());
            incidents = incidentRepository.findByTechnicianOrderByCreatedAtDesc(technician);
        } else {
            User resident = getRequiredUser(principal.getId());
            incidents = incidentRepository.findByResidentOrderByCreatedAtDesc(resident);
        }
        return incidents.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(Long incidentId) {
        Incident incident = getAccessibleIncident(incidentId, SecurityUtils.currentUser());
        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        AuthenticatedUser principal = SecurityUtils.currentUser();
        if (principal.getRole() != UserRole.RESIDENT) {
            throw new ForbiddenOperationException("Only residents can create incidents");
        }
        if (!Objects.equals(principal.getBuildingId(), request.buildingId())) {
            throw new ForbiddenOperationException("Residents can only create incidents for their own building");
        }

        User resident = getRequiredUser(principal.getId());
        Building building = buildingRepository.findById(request.buildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));

        Incident incident = new Incident();
        incident.setCode(generateIncidentCode());
        incident.setTitle(request.title().trim());
        incident.setDescription(request.description().trim());
        incident.setCategory(request.category());
        incident.setPriority(request.priority());
        incident.setStatus(IncidentStatus.PENDIENTE);
        incident.setResident(resident);
        incident.setBuilding(building);
        Incident saved = incidentRepository.save(incident);

        addStatusHistory(saved, null, IncidentStatus.PENDIENTE, resident, "Incident created");
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse assignTechnician(Long incidentId, IncidentAssignRequest request) {
        AuthenticatedUser principal = SecurityUtils.currentUser();
        if (principal.getRole() != UserRole.ADMIN) {
            throw new ForbiddenOperationException("Only admin can assign technicians");
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        User technician = getRequiredUser(request.technicianId());
        if (technician.getRole() != UserRole.TECHNICIAN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Assigned user must have TECHNICIAN role");
        }

        IncidentStatus previousStatus = incident.getStatus();
        validateTransition(previousStatus, IncidentStatus.ASIGNADA);

        incident.setTechnician(technician);
        incident.setStatus(IncidentStatus.ASIGNADA);
        incident.setAssignedAt(OffsetDateTime.now());
        Incident updated = incidentRepository.save(incident);

        User admin = getRequiredUser(principal.getId());
        addStatusHistory(updated, previousStatus, IncidentStatus.ASIGNADA, admin, "Technician assigned");
        addComment(updated, admin, CommentType.SYSTEM, "Administrador asigno tecnico: " + technician.getFullName());
        return toResponse(updated);
    }

    @Transactional
    public IncidentResponse updateStatus(Long incidentId, IncidentStatusUpdateRequest request) {
        AuthenticatedUser principal = SecurityUtils.currentUser();
        Incident incident = getAccessibleIncident(incidentId, principal);
        User actor = getRequiredUser(principal.getId());

        IncidentStatus currentStatus = incident.getStatus();
        IncidentStatus targetStatus = request.status();

        validateTransition(currentStatus, targetStatus);
        validateRoleCanChangeStatus(principal, incident, currentStatus, targetStatus, request.comment());

        if (targetStatus == IncidentStatus.CERRADA
                && !statusHistoryRepository.existsByIncidentIdAndToStatus(incident.getId(), IncidentStatus.RESUELTA)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot close incident before it reaches RESUELTA");
        }

        incident.setStatus(targetStatus);
        if (targetStatus == IncidentStatus.RESUELTA) {
            incident.setResolvedAt(OffsetDateTime.now());
        }
        if (targetStatus == IncidentStatus.CERRADA) {
            incident.setClosedAt(OffsetDateTime.now());
        }

        Incident updated = incidentRepository.save(incident);
        addStatusHistory(updated, currentStatus, targetStatus, actor, request.reason());

        if (request.comment() != null && !request.comment().isBlank()) {
            CommentType commentType = targetStatus == IncidentStatus.REABIERTA
                    ? CommentType.RESOLUTION_REJECTION
                    : CommentType.STATUS_CHANGE;
            addComment(updated, actor, commentType, request.comment());
        }

        return toResponse(updated);
    }

    @Transactional
    public IncidentCommentResponse addComment(Long incidentId, IncidentCommentRequest request) {
        Incident incident = getAccessibleIncident(incidentId, SecurityUtils.currentUser());
        User actor = getRequiredUser(SecurityUtils.currentUser().getId());
        IncidentComment comment = addComment(incident, actor, request.commentType(), request.message());
        return toCommentResponse(comment);
    }

    @Transactional
    public AttachmentResponse uploadAttachment(Long incidentId, MultipartFile file) {
        Incident incident = getAccessibleIncident(incidentId, SecurityUtils.currentUser());
        User actor = getRequiredUser(SecurityUtils.currentUser().getId());
        LocalStorageService.StoredFile storedFile = localStorageService.store(file);

        Attachment attachment = new Attachment();
        attachment.setIncident(incident);
        attachment.setUploadedBy(actor);
        attachment.setOriginalName(storedFile.originalName());
        attachment.setStoredName(storedFile.storedName());
        attachment.setStoragePath(storedFile.storagePath());
        attachment.setMimeType(storedFile.mimeType());
        attachment.setSizeBytes(storedFile.sizeBytes());
        attachment.setCreatedAt(OffsetDateTime.now());
        Attachment saved = attachmentRepository.save(attachment);

        return new AttachmentResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getStoragePath(),
                saved.getMimeType(),
                saved.getSizeBytes(),
                saved.getCreatedAt()
        );
    }

    private void validateRoleCanChangeStatus(
            AuthenticatedUser principal,
            Incident incident,
            IncidentStatus currentStatus,
            IncidentStatus targetStatus,
            String comment
    ) {
        if (principal.getRole() == UserRole.ADMIN) {
            return;
        }

        if (principal.getRole() == UserRole.TECHNICIAN) {
            if (incident.getTechnician() == null || !Objects.equals(incident.getTechnician().getId(), principal.getId())) {
                throw new ForbiddenOperationException("Technician can only update assigned incidents");
            }
            if (!Set.of(IncidentStatus.EN_PROCESO, IncidentStatus.RESUELTA, IncidentStatus.ASIGNADA).contains(targetStatus)) {
                throw new ForbiddenOperationException("Technician cannot set this status");
            }
            return;
        }

        if (!Set.of(IncidentStatus.REABIERTA, IncidentStatus.CERRADA).contains(targetStatus)) {
            throw new ForbiddenOperationException("Resident can only close or reopen incidents");
        }
        if (currentStatus != IncidentStatus.RESUELTA) {
            throw new ForbiddenOperationException("Resident can only close/reopen from RESUELTA");
        }
        if (targetStatus == IncidentStatus.REABIERTA && (comment == null || comment.isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Comment is mandatory when reopening an incident");
        }
    }

    private void validateTransition(IncidentStatus from, IncidentStatus to) {
        Set<IncidentStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowedTargets.contains(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid status transition: " + from + " -> " + to);
        }
    }

    private Incident getAccessibleIncident(Long incidentId, AuthenticatedUser principal) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        if (principal.getRole() == UserRole.ADMIN) {
            return incident;
        }
        if (principal.getRole() == UserRole.RESIDENT && Objects.equals(incident.getResident().getId(), principal.getId())) {
            return incident;
        }
        if (principal.getRole() == UserRole.TECHNICIAN
                && incident.getTechnician() != null
                && Objects.equals(incident.getTechnician().getId(), principal.getId())) {
            return incident;
        }
        throw new ForbiddenOperationException("You are not allowed to access this incident");
    }

    private User getRequiredUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private String generateIncidentCode() {
        String datePart = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = new Random().nextInt(9000) + 1000;
        return "INC-" + datePart + "-" + randomPart;
    }

    private void addStatusHistory(
            Incident incident,
            IncidentStatus fromStatus,
            IncidentStatus toStatus,
            User changedBy,
            String reason
    ) {
        IncidentStatusHistory history = new IncidentStatusHistory();
        history.setIncident(incident);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        history.setCreatedAt(OffsetDateTime.now());
        statusHistoryRepository.save(history);
    }

    private IncidentComment addComment(Incident incident, User author, CommentType commentType, String message) {
        IncidentComment comment = new IncidentComment();
        comment.setIncident(incident);
        comment.setAuthor(author);
        comment.setCommentType(commentType);
        comment.setMessage(message);
        comment.setCreatedAt(OffsetDateTime.now());
        return incidentCommentRepository.save(comment);
    }

    private IncidentResponse toResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getCode(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getCategory(),
                incident.getPriority(),
                incident.getStatus(),
                incident.isOverdue(),
                incident.getResident().getId(),
                incident.getTechnician() != null ? incident.getTechnician().getId() : null,
                incident.getBuilding().getId(),
                incident.getCreatedAt(),
                incident.getAssignedAt(),
                incident.getResolvedAt(),
                incident.getClosedAt()
        );
    }

    private IncidentCommentResponse toCommentResponse(IncidentComment comment) {
        return new IncidentCommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getCommentType(),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }

    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getStoragePath(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getCreatedAt()
        );
    }

    private IncidentStatusHistoryResponse toStatusHistoryResponse(IncidentStatusHistory history) {
        return new IncidentStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy().getId(),
                history.getReason(),
                history.getCreatedAt()
        );
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
