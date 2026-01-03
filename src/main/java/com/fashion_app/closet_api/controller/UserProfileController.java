package com.fashion_app.closet_api.controller;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.dto.UserProfileResponse;
import com.fashion_app.closet_api.service.FileStorageService;
import com.fashion_app.closet_api.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> createProfile(
            @AuthenticationPrincipal User user,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "bodyShape", required = false) String bodyShape,
            @RequestPart("preferences") List<Map<String, Object>> preferences
    ) {
        UserProfileResponse response = userProfileService.createProfile(user, image, bodyShape, preferences);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "bodyShape", required = false) String bodyShape,
            @RequestPart(value = "preferences", required = false) List<Map<String, Object>> preferences
    ) {
        UserProfileResponse response = userProfileService.updateProfile(user, image, bodyShape, preferences);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userProfileService.getProfile(user));
    }

//    @DeleteMapping
//    public void deleteProfile(@AuthenticationPrincipal User user) {
//        userProfileService.deleteProfile(user);
//    }

//    @DeleteMapping("/image")
//    public ResponseEntity<Void> deleteBodyShapeImage(@AuthenticationPrincipal User user) {
//        userProfileService.removeBodyShapeImage(user);
//        return ResponseEntity.noContent().build();
//    }
}
