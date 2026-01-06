package com.fashion_app.closet_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OutfitItemData {
    private Long id;
    private int layerOrder;
    private String transformData;
}
