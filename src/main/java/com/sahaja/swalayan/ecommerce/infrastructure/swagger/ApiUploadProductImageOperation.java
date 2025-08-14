package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sahaja.swalayan.ecommerce.application.dto.ProductImageUploadRequest;

@Operation(
    summary = "Upload product image",
    description = "Uploads an image for the specified product. Accepts only image files (png, jpeg, etc.) up to 5MB. On success, updates the product's imageUrl field and returns the updated product.",
    requestBody = @RequestBody(
        description = "Image file to upload (max 5MB, only image types allowed)",
        required = true,
        content = @Content(
            mediaType = "multipart/form-data",
            schema = @Schema(
                implementation = ProductImageUploadRequest.class
            )
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Image uploaded and product updated",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Image Upload Success",
                value = "{\n  \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n  \"name\": \"Samsung Galaxy S24\",\n  \"description\": \"Latest flagship smartphone\",\n  \"price\": 999.99,\n  \"quantity\": 50,\n  \"category_id\": \"456e7890-e89b-12d3-a456-426614174001\",\n  \"weight\": 168,\n  \"height\": 147,\n  \"length\": 71,\n  \"width\": 8,\n  \"imageUrl\": \"/uploads/products/123e4567-e89b-12d3-a456-426614174000/abc123.png\"\n}"
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid file type or file too large. Only image files up to 5MB are allowed.",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Invalid File Type",
                    value = "{\n  \"success\": false,\n  \"message\": \"Only image files are allowed\",\n  \"timestamp\": \"2025-08-03T06:58:46\"\n}"
                ),
                @ExampleObject(
                    name = "File Too Large",
                    value = "{\n  \"success\": false,\n  \"message\": \"File size exceeds 5MB limit\",\n  \"timestamp\": \"2025-08-03T06:58:46\"\n}"
                )
            }
        )
    ),
    @ApiResponse(responseCode = "404", description = "Product not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ApiUploadProductImageOperation {
}
