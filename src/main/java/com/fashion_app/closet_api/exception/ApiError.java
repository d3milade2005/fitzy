package com.fashion_app.closet_api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        int status,
        String path,
        Instant timestamp
) {}
