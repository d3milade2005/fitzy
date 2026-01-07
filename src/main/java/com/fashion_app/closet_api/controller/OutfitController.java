package com.fashion_app.closet_api.controller;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.dto.OutfitRequest;
import com.fashion_app.closet_api.dto.OutfitResponse;
import com.fashion_app.closet_api.service.OutfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveOutfit(@AuthenticationPrincipal User user, @RequestBody OutfitRequest request) {
        String outfitLink = outfitService.saveOutfit(user, request);
        Map<String, Object> response = Map.of("message", "Outfit saved successfully", "outfitLink", outfitLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOutfit(@AuthenticationPrincipal User user, @RequestParam("outfitLink") String outfitLink) {
        OutfitResponse outfit = outfitService.getOutfit(user, outfitLink);
        Map<String, Object> response = Map.of("outfit", outfit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOutfit(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("outfits", outfitService.getAllOutfit(user)));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteOutfit(@RequestParam("outfitId") Long outfitId) {
        outfitService.deleteOutfit(outfitId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "Outfit deleted successfully"));
    }
}
