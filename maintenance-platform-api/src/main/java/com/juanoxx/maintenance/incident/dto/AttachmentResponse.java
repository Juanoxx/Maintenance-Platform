package com.juanoxx.maintenance.incident.dto;

import java.time.OffsetDateTime;

public record AttachmentResponse(
        Long id,
        String originalName,
        String storagePath,
        String mimeType,
        Long sizeBytes,
        OffsetDateTime createdAt
) {
}
