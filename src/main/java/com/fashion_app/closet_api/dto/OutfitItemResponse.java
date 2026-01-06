package com.fashion_app.closet_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OutfitItemResponse {
    private Long itemId;
    private int layerOrder;
    private String transformData;
}
