package com.fashion_app.closet_api.controller;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.service.FashionCalendarService;
import com.fashion_app.closet_api.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleAuthService googleAuthService;
    private final FashionCalendarService fashionService;

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        String url = googleAuthService.getAuthorizationUrl(userId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String stateUserId) {
        try {
            googleAuthService.linkGoogleAccount(code, stateUserId);
            return ResponseEntity.ok("Google Calendar successfully connected! You may close this tab.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Connection failed: " + e.getMessage());
        }
    }

    // This particular process has to be a background job. Tell user their calendar is being synced. Ran a job to do the processing and when done send a notification
    // use celery, redis and fcm(firebase cloud messaging)
    @PostMapping("/sync")
    public ResponseEntity<String> syncCalendar(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        fashionService.generateOutfitsForUser(userId);
        return ResponseEntity.ok("Sync started! Check your Google Calendar in a few seconds.");
    }

    private UUID getCurrentUserId(Authentication authentication) {
         User user = (User) authentication.getPrincipal();
         return user.getId();
    }
}