package com.fashion_app.closet_api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserProfileRequest {
    private List<Map<String, Object>> stylePreferences;
}