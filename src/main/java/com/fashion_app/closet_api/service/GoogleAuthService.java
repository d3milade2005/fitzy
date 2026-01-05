package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Repository.UserRepository;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {
    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private GoogleAuthorizationCodeFlow flow;
    private NetHttpTransport httpTransport;

    @PostConstruct
    public void init() throws Exception {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 1. Setup Google Client Secrets
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(clientId);
        web.setClientSecret(clientSecret);
        GoogleClientSecrets secrets = new GoogleClientSecrets().setWeb(web);

        // 2. Create the Flow (Manages the OAuth Exchange)
        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GsonFactory.getDefaultInstance(), secrets,
                Collections.singleton(CalendarScopes.CALENDAR)) // Scope: Read/Write Calendar
                .setAccessType("offline") // CRITICAL: Asks for Refresh Token
                .setApprovalPrompt("force") // CRITICAL: Forces user to re-consent if they disconnected
                .build();
    }

    public String getAuthorizationUrl(UUID userId) {
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(userId.toString())
                .build();
    }

    @Transactional
    public void linkGoogleAccount(String code, String stateUserId) throws IOException {
        UUID userId = UUID.fromString(stateUserId);

        // Exchange "Code" for "Tokens"
        TokenResponse response = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "User not found"
                ));

        user.setGoogleAccessToken(response.getAccessToken());
        if (response.getRefreshToken() != null) {
            user.setGoogleRefreshToken(response.getRefreshToken());
        }

        long expiryTime = System.currentTimeMillis() + (response.getExpiresInSeconds() * 1000);
        user.setGoogleTokenExpiry(new Timestamp(expiryTime).toLocalDateTime());

        userRepository.save(user);
        log.info("Linked Google Account for User: {}", userId);
    }

    @Transactional
    public Calendar getCalendarService(UUID userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.getGoogleRefreshToken() == null) {
            throw new BusinessException(ErrorCode.CALENDAR_API_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to Google Calendar API.");
        }

        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
                .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                .build();

        credential.setAccessToken(user.getGoogleAccessToken());
        credential.setRefreshToken(user.getGoogleRefreshToken());

        if (user.getGoogleTokenExpiry() != null) {
            credential.setExpirationTimeMilliseconds(user.getGoogleTokenExpiry().atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }

        // Check if Token is Expired (or expiring in < 60 seconds)
        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() < 60) {
            log.info("Access token expired for user {}. Refreshing...", userId);

            boolean success = credential.refreshToken();
            if (success) {
                user.setGoogleAccessToken(credential.getAccessToken());
                user.setGoogleTokenExpiry(new Timestamp(credential.getExpirationTimeMilliseconds()).toLocalDateTime());
                userRepository.save(user);
            } else {
                throw new IOException("Failed to refresh access token. Please reconnect account.");
            }
        }

        return new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Closet App")
                .build();
    }
}
