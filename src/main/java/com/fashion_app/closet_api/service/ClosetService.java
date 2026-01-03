package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.Category;
import com.fashion_app.closet_api.Entity.ClosetItem;
import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Repository.ClosetRepository;
import com.fashion_app.closet_api.dto.ClosetResponse;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClosetService {
    private final ClosetRepository closetRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ClosetResponse addItem(User user, MultipartFile image, String category) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Image is required");
        }
        validateImage(image);

        String imageUrl = fileStorageService.saveFile(image, "closet-items");

        ClosetItem item = ClosetItem.builder()
                .user(user)
                .imageUrl(imageUrl)
                .category(Category.valueOf(category.toUpperCase()))
                .uploadedAt(LocalDateTime.now())
                .build();

        ClosetItem savedItem = closetRepository.save(item);
        return mapToResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public Page<ClosetResponse> getUserItems(User user, String categoryName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<ClosetItem> itemPage;

        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = Category.valueOf(categoryName.toUpperCase());
            itemPage = closetRepository.findAllByUserIdAndCategory(user.getId(), category, pageable);
        } else {
            itemPage = closetRepository.findAllByUserId(user.getId(), pageable);
        }
        return itemPage.map(this::mapToResponse);
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

    private ClosetResponse mapToResponse(ClosetItem item) {
        return ClosetResponse.builder()
                .id(item.getId())
                .imageUrl(item.getImageUrl())
                .category(item.getCategory())
                .season(item.getSeason())
                .description(item.getAiDescription())
                .uploadedAt(item.getUploadedAt())
                .build();
    }
}
