package com.fashion_app.closet_api.dto;

public record ApiResponse<T>(String message, T data) {}
