package com.juanoxx.maintenance.common.model;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
