package com.sahaja.swalayan.ecommerce.application.controller;

import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;

import jakarta.validation.Valid;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Category API", description = "Operations related to product categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    @Operation(
        summary = "Create a new category",
        description = "Creates a new product category with the provided details.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Category details to create",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class),
                examples = @ExampleObject(
                    name = "Category Creation Example",
                    value = """
                    {
                        "name": "Electronics",
                        "description": "Electronic devices and accessories"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Category created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid category data provided",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category saved = categoryService.save(categoryMapper.toEntity(categoryDTO));
        return new ResponseEntity<>(categoryMapper.toDTO(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieves a specific category by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Category found successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Category not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<CategoryDTO> getCategoryById(
        @Parameter(description = "Category ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }

    @GetMapping
    @Operation(
        summary = "Get all categories",
        description = "Retrieves a list of all available product categories."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class)
            )
        )
    })
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        List<CategoryDTO> dtos = categoryMapper.toDTOList(categories);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing category",
        description = "Updates an existing category with the provided details.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated category details",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class),
                examples = @ExampleObject(
                    name = "Category Update Example",
                    value = """
                    {
                        "name": "Home & Garden",
                        "description": "Home improvement and gardening supplies"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Category updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid category data provided",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Category not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<CategoryDTO> updateCategory(
        @Parameter(description = "Category ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id, 
        @Valid @RequestBody CategoryDTO categoryDTO) {
        Category updated = categoryService.update(id, categoryMapper.toEntity(categoryDTO));
        return ResponseEntity.ok(categoryMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a category",
        description = "Deletes a category by its unique identifier. This operation cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Category deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Category not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Category cannot be deleted due to existing dependencies",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<Void> deleteCategory(
        @Parameter(description = "Category ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search for a category by name",
        description = "Finds a category by its exact name. The search is case-sensitive."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Category found successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Category not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<CategoryDTO> getCategoryByName(
        @Parameter(description = "Category name to search for", required = true, example = "Electronics")
        @RequestParam String name) {
        Category category = categoryService.findByName(name);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }
}
