package com.fashion_app.closet_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileStorageService {
    String uploadFile(MultipartFile file);
    String getFileUrl(String fileName);
    void deleteFile(String fileUrl);
}