package com.sporthub.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", request, fields);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", request, List.of());
    }

    private ApiErrorResponse.FieldError toFieldError(FieldError error) {
        return new ApiErrorResponse.FieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message,
                                                       HttpServletRequest request, List<ApiErrorResponse.FieldError> fields) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(OffsetDateTime.now(), status.value(), code,
                message, request.getRequestURI(), fields));
    }
}
