package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.ClosetItem;
import com.fashion_app.closet_api.Entity.Outfit;
import com.fashion_app.closet_api.Entity.OutfitItem;
import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Repository.ClosetRepository;
import com.fashion_app.closet_api.Repository.OutfitRepository;
import com.fashion_app.closet_api.Repository.UserRepository;
import com.fashion_app.closet_api.dto.*;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OutfitService {

    private final OutfitRepository outfitRepository;
    private final ClosetRepository closetRepository;
    private final UserRepository userRepository;
    private final GcsFileStorageService fileStorageService;

    public String saveOutfit(OutfitRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        Outfit outfit = new Outfit();
        outfit.setName(request.getName());
        outfit.setUser(user);
        outfit.setCreatedAt(LocalDateTime.now());
        outfit.setOutfitLink(generateOutfitLink());
        outfit.setOutfitItems(new HashSet<>());

        for (OutfitItemData itemData : request.getOutfitItemData()) {
            ClosetItem closetItem = closetRepository.findById(itemData.getItemId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.OUTFIT_NOT_FOUND, HttpStatus.NOT_FOUND, "Outfit item not found"));

            OutfitItem outfitItem = new OutfitItem();
            outfitItem.setItem(closetItem);
            outfitItem.setOutfit(outfit);
            outfitItem.setLayerOrder(itemData.getLayerOrder());
            outfitItem.setTransformData(itemData.getTransformData());

            outfit.getOutfitItems().add(outfitItem);
        }
        outfitRepository.save(outfit);
        return outfit.getOutfitLink();
    }

    public OutfitResponse getOutfit(String outfitLink) {
        Outfit outfit = outfitRepository.findByOutfitLink(outfitLink)
                .orElseThrow(() -> new BusinessException(ErrorCode.OUTFIT_NOT_FOUND, HttpStatus.NOT_FOUND, "Full Outfit not found"));
        return mapToResponse(outfit);
    }

    public List<OutfitSummary> getAllOutfit(User user) {
        return outfitRepository.findByUserId(user.getId())
                .stream()
                .map(o -> new OutfitSummary(o.getId(), o.getName()))
                .toList();
    }

    @Transactional
    public void deleteOutfit(Long outfitId) {
        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.OUTFIT_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Outfit not found"
                ));
        outfitRepository.delete(outfit);
    }


    private OutfitResponse mapToResponse(Outfit outfit) {
        List<OutfitItemResponse> items = outfit.getOutfitItems()
                .stream()
                .sorted(Comparator.comparingInt(OutfitItem::getLayerOrder))
                .map(oi -> {
                    OutfitItemResponse dto = new OutfitItemResponse();
                    dto.setItemId(oi.getItem().getId());
                    dto.setLayerOrder(oi.getLayerOrder());
                    dto.setTransformData(oi.getTransformData());
                    return dto;
                })
                .toList();

        return OutfitResponse.builder()
                .id(outfit.getId())
                .name(outfit.getName())
                .outfitItems(items)
                .build();
    }


    private String generateOutfitLink() {
        return UUID.randomUUID().toString();
    }
}
