package com.fashion_app.closet_api.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "outfits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Outfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true, nullable = false)
    private String outfitLink;

    @OneToMany(
            mappedBy = "outfit",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<OutfitItem> outfitItems = new HashSet<>();

    @Column(nullable = true)
    private String googleEventId;

    @Column(nullable = true)
    private LocalDateTime eventStart;

    @Column(nullable = true)
    private LocalDateTime eventEnd;

    private LocalDateTime createdAt;
}
