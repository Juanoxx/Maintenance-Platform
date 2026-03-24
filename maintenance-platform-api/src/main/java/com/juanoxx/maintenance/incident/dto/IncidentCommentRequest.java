package com.juanoxx.maintenance.incident.dto;

import com.juanoxx.maintenance.incident.entity.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentCommentRequest(
        @NotBlank @Size(max = 2500) String message,
        @NotNull CommentType commentType
) {
}
