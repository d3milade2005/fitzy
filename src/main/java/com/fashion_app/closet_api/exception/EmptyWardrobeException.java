package com.fashion_app.closet_api.exception;

import org.springframework.http.HttpStatus;

public class EmptyWardrobeException extends BusinessException {
    public EmptyWardrobeException() {
        super(ErrorCode.EMPTY_WARDROBE, HttpStatus.BAD_REQUEST,
                "Upload at least 1 Top and 1 Bottom to get started");
    }
}
