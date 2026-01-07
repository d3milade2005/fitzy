package com.fashion_app.closet_api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OutfitResponse {
    private Long id;
    private String name;
    private List<OutfitItemResponse> outfitItems;
    private String googleEventId;
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
}

