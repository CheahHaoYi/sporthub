package com.sporthub.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(OffsetDateTime timestamp, int status, String code, String message,
                               String path, List<FieldError> fieldErrors) {
    public record FieldError(String field, String message) { }
}
