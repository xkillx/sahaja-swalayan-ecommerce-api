package com.sahaja.swalayan.ecommerce.application.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Category data transfer object")
public class CategoryDTO {
    @Schema(description = "Unique identifier for the category", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Name of the category", example = "Electronics", required = true, maxLength = 255)
    @NotBlank(message = "Category name must not be blank")
    @Size(max = 255, message = "Category name must be at most 255 characters")
    private String name;

    @Schema(description = "Description of the category", example = "Electronic devices and accessories")
    private String description;
}
