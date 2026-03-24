package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.CommentType;

import java.time.OffsetDateTime;

public record IncidentCommentResponse(
        Long id,
        Long authorId,
        CommentType commentType,
        String message,
        OffsetDateTime createdAt
) {
}
