package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Entity.UserProfile;
import com.fashion_app.closet_api.Repository.UserProfileRepository;
import com.fashion_app.closet_api.dto.UserProfileResponse;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public UserProfileResponse createProfile(User user, MultipartFile bodyImage, List<Map<String, Object>> stylePreferences) {
        if (userProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    HttpStatus.CONFLICT,
                    "User profile already exists. Use PUT to update."
            );
        }

        validateImage(bodyImage);

        String imageUrl = fileStorageService.saveFile(bodyImage, "user-profiles");

        UserProfile profile = UserProfile.builder()
                .user(user)
                .bodyShapeImageUrl(imageUrl)
                .stylePreferences(stylePreferences)
                .build();

        UserProfile saved = userProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, MultipartFile bodyImage, List<Map<String, Object>> preferences) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        HttpStatus.NOT_FOUND,
                        "Profile not found. Please create one first."
                ));


        if (bodyImage != null && !bodyImage.isEmpty()) {
            validateImage(bodyImage);
            String newImageUrl = fileStorageService.saveFile(bodyImage, "user-profiles");
            profile.setBodyShapeImageUrl(newImageUrl);
        }

        if (preferences != null && !preferences.isEmpty()) {
            profile.setStylePreferences(preferences);
        }

        UserProfile saved = userProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    public UserProfileResponse getProfile(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "User profile not set up."
                ));

        return mapToResponse(profile);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    "Body reference image is required."
            );
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    "File must be an image."
            );
        }
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .bodyShapeImageUrl(profile.getBodyShapeImageUrl())
                .stylePreferences(profile.getStylePreferences())
                .build();
    }
}
