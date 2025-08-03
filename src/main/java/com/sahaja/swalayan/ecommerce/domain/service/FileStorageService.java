package com.sahaja.swalayan.ecommerce.domain.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Stores the file and returns the relative URL/path for access.
     */
    String storeProductImage(MultipartFile file, String productId);
}
