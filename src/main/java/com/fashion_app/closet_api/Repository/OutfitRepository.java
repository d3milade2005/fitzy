package com.fashion_app.closet_api.Repository;

import com.fashion_app.closet_api.Entity.Outfit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OutfitRepository extends JpaRepository<Outfit, Long> {
    Optional<Outfit> findByUserId(Long userId);
    Optional<Outfit> findByOutfitLink(String outfitLink);
}
