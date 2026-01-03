package com.fashion_app.closet_api.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GcsFileStorageService implements FileStorageService{

    private final Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = UUID.randomUUID().toString() + extension;

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        try {
            storage.create(blobInfo, file.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file input stream", e);
        } catch (Exception e) {
            throw new RuntimeException("GCS Upload error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        // Note: This requires a Service Account Key (JSON) to be configured.
        // It won't work with just "gcloud auth login" credentials locally.
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, key).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                12,
                TimeUnit.HOURS,
                Storage.SignUrlOption.withV4Signature()
        );

        return signedUrl.toString();
    }

    public void deleteFile(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        BlobId blobId = BlobId.of(bucketName, key);
        storage.delete(blobId);
    }
}
