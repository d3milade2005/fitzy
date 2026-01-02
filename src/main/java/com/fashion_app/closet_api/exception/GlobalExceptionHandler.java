package com.fashion_app.closet_api.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiError error = new ApiError(
                "BAD_CREDENTIALS",
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED.value(), // 401
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 2. Handle JWT Specific Errors (Expired or Tampered token)
    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class})
    public ResponseEntity<ApiError> handleJwtException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.warn("JWT Error: {}", ex.getMessage());

        ApiError error = new ApiError(
                "INVALID_TOKEN",
                "Token has expired or is invalid",
                HttpStatus.FORBIDDEN.value(), // 403
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("User not found: {}", ex.getMessage());

        ApiError error = new ApiError(
                "USER_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(), // 404
                request.getRequestURI(),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}