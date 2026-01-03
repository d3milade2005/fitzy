package com.fashion_app.closet_api.dto;

import com.fashion_app.closet_api.Entity.Category;
import com.fashion_app.closet_api.Entity.Season;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClosetResponse {
    private Long id;
    private String imageUrl;
    private Category category;
    private Season season;
    private String description;
    private LocalDateTime uploadedAt;
}
