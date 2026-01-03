package com.fashion_app.closet_api.controller;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.dto.ClosetResponse;
import com.fashion_app.closet_api.service.ClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/closet")
@RequiredArgsConstructor
public class ClosetController {
    private final ClosetService closetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClosetResponse> uploadItem(
            @AuthenticationPrincipal User user,
            @RequestPart("image") MultipartFile image,
            @RequestPart("category") String category
    ) {
        ClosetResponse response = closetService.addItem(user, image, category);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ClosetResponse>> getMyCloset(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ClosetResponse> closetPage = closetService.getUserItems(user, category, page, size);
        return ResponseEntity.ok(closetPage);
    }
}
