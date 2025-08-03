package com.sahaja.swalayan.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for product image upload via multipart/form-data.
 */
@Data
public class ProductImageUploadRequest {
    @Schema(
        type = "string",
        format = "binary",
        description = "Image file (png, jpg, jpeg, gif)"
    )
    private MultipartFile file;
}
