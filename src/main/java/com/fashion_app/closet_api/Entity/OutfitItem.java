package com.fashion_app.closet_api.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="outfit_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutfitItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    private Outfit outfit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ClosetItem item;

    private int layerOrder; // 1 = bottom, higher = top

    // Optional JSON for future 3D transformations / AI adjustments
    @Column(columnDefinition = "jsonb")
    private String transformData;
}