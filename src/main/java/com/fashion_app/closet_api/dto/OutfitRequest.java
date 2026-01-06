package com.fashion_app.closet_api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OutfitRequest {
    private UUID userId;
    private String name;
    private List<OutfitItemData> outfitItemData;
}