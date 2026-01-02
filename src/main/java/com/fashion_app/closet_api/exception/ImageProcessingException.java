package com.fashion_app.closet_api.exception;

import org.springframework.http.HttpStatus;

public class ImageProcessingException extends BusinessException {

    public ImageProcessingException(String itemId) {
        super(ErrorCode.IMAGE_PROCESSING_FAILED, HttpStatus.INTERNAL_SERVER_ERROR,
                "Background removal failed for item: " + itemId);
    }
}