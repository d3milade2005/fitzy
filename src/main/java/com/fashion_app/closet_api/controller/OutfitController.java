package com.fashion_app.closet_api.controller;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.dto.OutfitRequest;
import com.fashion_app.closet_api.service.OutfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveOutfit(@AuthenticationPrincipal User user, @RequestBody OutfitRequest request) {
        String outfitLink = outfitService.saveOutfit(request);
        Map<String, Object> response = Map.of("message", "Outfit saved successfully", "outfitLink", outfitLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
