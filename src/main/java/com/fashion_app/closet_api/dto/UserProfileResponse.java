package com.fashion_app.closet_api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserProfileResponse {
    private String bodyShapeImageUrl;
    private List<Map<String, Object>> stylePreferences;
}
