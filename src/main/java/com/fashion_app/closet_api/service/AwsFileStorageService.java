package com.fashion_app.closet_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "aws")
public class AwsFileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
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

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType()) // Crucial for browser rendering
                .build();

        try {
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file input stream", e);
        } catch (S3Exception e) {
            throw new RuntimeException("S3 Upload error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {

        }
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(12))
                .getObjectRequest(objectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
    }
}
