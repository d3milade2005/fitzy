package com.fashion_app.closet_api.Repository;

import com.fashion_app.closet_api.Entity.Category;
import com.fashion_app.closet_api.Entity.ClosetItem;
import com.fashion_app.closet_api.Entity.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClosetRepository extends JpaRepository<ClosetItem, Long> {
    Page<ClosetItem> findAllByUserId(UUID userId, Pageable pageable);
    Page<ClosetItem> findAllByUserIdAndCategory(UUID userId, Category category, Pageable pageable);
    Page<ClosetItem> findAllByUserIdAndDetectedSeason(UUID userId, Season season, Pageable pageable);
}
