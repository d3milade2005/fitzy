package com.fashion_app.closet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutfitSummary {
    private Long id;
    private String name;
    private String googleEventId;
}
