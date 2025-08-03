package com.sahaja.swalayan.ecommerce.application.service;

import lombok.extern.slf4j.Slf4j;
import com.sahaja.swalayan.ecommerce.common.FileStorageException;
import com.sahaja.swalayan.ecommerce.domain.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Override
    public String storeProductImage(MultipartFile file, String productId) {
        try {
            log.debug("Storing product image for productId={} originalFilename={} contentType={} size={} bytes", productId, file.getOriginalFilename(), file.getContentType(), file.getSize());
            // Validate file type (allow only images)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.debug("Rejected non-image file for productId={} filename={}", productId, file.getOriginalFilename());
                throw new FileStorageException("Only image files are allowed");
            }
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.debug("Rejected oversized file for productId={} filename={} size={} bytes", productId, file.getOriginalFilename(), file.getSize());
                throw new FileStorageException("File size exceeds 5MB limit");
            }
            // Resolve upload directory to absolute path if necessary
            Path dirPath = Paths.get(uploadDir);
            if (!dirPath.isAbsolute()) {
                dirPath = Paths.get(System.getProperty("user.dir")).resolve(uploadDir).toAbsolutePath();
            }
            log.debug("Resolved upload directory: {}", dirPath);
            // Ensure parent directories exist
            Files.createDirectories(dirPath);
            // Use productId as filename
            String ext = extractExtension(file.getOriginalFilename());
            String filename = productId + (ext.isEmpty() ? "" : "." + ext);
            Path filePath = dirPath.resolve(filename);
            // Save file
            file.transferTo(filePath.toFile());
            log.debug("Image stored at {} for productId={}", filePath, productId);
            // Return relative path (can be used as URL)
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            log.debug("Failed to store image for productId={}: {}", productId, e.getMessage());
            throw new FileStorageException("Failed to store file", e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1) : "";
    }
}
