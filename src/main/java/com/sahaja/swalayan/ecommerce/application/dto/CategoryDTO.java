package com.sahaja.swalayan.ecommerce.application.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private UUID id;

    @NotBlank(message = "Category name must not be blank")
    @Size(max = 255, message = "Category name must be at most 255 characters")
    private String name;

    private String description;
}
