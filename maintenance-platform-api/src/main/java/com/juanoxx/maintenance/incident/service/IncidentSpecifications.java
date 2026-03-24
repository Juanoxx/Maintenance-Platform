package com.juanoxx.maintenance.incident.service;

import com.juanoxx.maintenance.incident.dto.IncidentSearchCriteria;
import com.juanoxx.maintenance.incident.entity.Incident;
import com.juanoxx.maintenance.security.model.AuthenticatedUser;
import com.juanoxx.maintenance.user.entity.UserRole;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> visibleTo(AuthenticatedUser principal) {
        if (principal.getRole() == UserRole.ADMIN) {
            return all();
        }
        if (principal.getRole() == UserRole.TECHNICIAN) {
            return (root, query, cb) -> cb.equal(root.get("technician").get("id"), principal.getId());
        }
        return (root, query, cb) -> cb.equal(root.get("resident").get("id"), principal.getId());
    }

    public static Specification<Incident> byCriteria(IncidentSearchCriteria criteria) {
        Specification<Incident> spec = all();
        if (criteria == null) {
            return spec;
        }

        if (criteria.buildingId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("building").get("id"), criteria.buildingId()));
        }
        if (criteria.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.status()));
        }
        if (criteria.priority() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), criteria.priority()));
        }
        if (criteria.category() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), criteria.category()));
        }
        if (criteria.technicianId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("technician").get("id"), criteria.technicianId()));
        }
        if (criteria.overdue() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("overdue"), criteria.overdue()));
        }
        if (criteria.createdFrom() != null) {
            OffsetDateTime fromInclusive = toStartOfDay(criteria.createdFrom());
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromInclusive));
        }
        if (criteria.createdTo() != null) {
            OffsetDateTime toExclusive = toStartOfDay(criteria.createdTo().plusDays(1));
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), toExclusive));
        }
        return spec;
    }

    private static OffsetDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private static Specification<Incident> all() {
        return (root, query, cb) -> cb.conjunction();
    }
}
