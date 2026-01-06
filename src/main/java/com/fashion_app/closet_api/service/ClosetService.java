package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.Category;
import com.fashion_app.closet_api.Entity.ClosetItem;
import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Repository.ClosetRepository;
import com.fashion_app.closet_api.dto.ClosetResponse;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import com.fashion_app.closet_api.util.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClosetService {
    private final ClosetRepository closetRepository;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;

    @Transactional
    public ClosetResponse addItem(User user, MultipartFile image, String category) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Image is required");
        }
        fileValidator.validateImage(image);

        String imageKey = fileStorageService.uploadFile(image);

        ClosetItem item = ClosetItem.builder()
                .user(user)
                .imageKey(imageKey)
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

    @Transactional
    public void deleteItem(Long id, User user) {
        ClosetItem item = closetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.OUTFIT_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Outfit not found"
                ));

        if (!item.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "You are not authorized to delete this outfit");
        }

        String ImageKey = item.getImageKey();
        try {
            closetRepository.deleteById(id);
            fileStorageService.deleteFile(ImageKey);
        } catch (Exception e) {
            log.error("Error deleting outfit: {}", e.getMessage());
        }
    }

    private ClosetResponse mapToResponse(ClosetItem item) {
        return ClosetResponse.builder()
                .id(item.getId())
                .imageKey(item.getImageKey())
                .imageUrl(fileStorageService.getFileUrl(item.getImageKey()))
                .category(item.getCategory())
                .season(item.getSeason())
                .description(item.getAiDescription())
                .uploadedAt(item.getUploadedAt())
                .build();
    }
}
