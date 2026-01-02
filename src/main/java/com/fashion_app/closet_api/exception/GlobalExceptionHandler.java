package com.fashion_app.closet_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ApiError error = new ApiError(
                ex.getErrorCode().name(),
                ex.getMessage(),
                ex.getStatus().value(),
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<ApiError> handleExternalServiceException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("External service failure: {}", ex.getMessage(), ex);

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.name(),
                "External service unavailable, try again later",
                503,
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(503).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnhandledException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred",
                500,
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(500).body(error);
    }
}