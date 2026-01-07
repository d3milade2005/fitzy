package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
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
            return fileName; // Return the media link: Blob blob = storage.create(...); blob.getMediaLink OR String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
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

//    public byte[] downloadFile(String bucketName, String fileName) {
//        Blob blob = storage.get(BlobId.of(bucketName, fileName));
//        if (blob == null) {
//            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, HttpStatus.NOT_FOUND, "File not found in bucket: " + fileName);
//        }
//        return blob.getContent();
//    }

    public void deleteFile(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        BlobId blobId = BlobId.of(bucketName, key);
        storage.delete(blobId);
    }
}
