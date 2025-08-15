package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.CategoryMapper;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;
import com.sahaja.swalayan.ecommerce.domain.service.CategorySearchService;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Category API", description = "Operations related to product categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final CategorySearchService categorySearchService;

    @PostMapping
    @ApiCreateCategoryOperation
    @ApiSuccessResponseWithExample(
        description = "Category created successfully",
        exampleName = "Created Category",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Electronics",
            "description": "Electronic devices and accessories including smartphones, laptops, and gadgets",
            "createdAt": "2025-01-21T12:56:03",
            "updatedAt": "2025-01-21T12:56:03"
        }
        """
    )
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category saved = categoryService.save(categoryMapper.toEntity(categoryDTO));
        return new ResponseEntity<>(categoryMapper.toDTO(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieves a specific category by its unique identifier.",
        parameters = @Parameter(
            name = "id",
            description = "Unique identifier of the category to retrieve",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
    )
    @ApiSuccessResponseWithExample(
        description = "Category found successfully",
        exampleName = "Category Details",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Electronics",
            "description": "Electronic devices and accessories including smartphones, laptops, and gadgets",
            "createdAt": "2025-01-21T12:56:03",
            "updatedAt": "2025-01-21T12:56:03"
        }
        """
    )
    @ApiNotFoundResponse
    @ApiServerErrorResponse
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable UUID id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(categoryMapper.toDTO(category));
    }

    @GetMapping
    @Operation(
        summary = "Get all categories",
        description = "Retrieves a list of all available product categories."
    )
    @ApiSuccessResponseWithExample(
        description = "Categories retrieved successfully",
        exampleName = "Category List",
        example = """
        [
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Electronics",
                "description": "Electronic devices and accessories",
                "createdAt": "2025-01-21T12:56:03",
                "updatedAt": "2025-01-21T12:56:03"
            },
            {
                "id": "456e7890-e89b-12d3-a456-426614174001",
                "name": "Clothing",
                "description": "Fashion and apparel items",
                "createdAt": "2025-01-21T12:56:03",
                "updatedAt": "2025-01-21T12:56:03"
            }
        ]
        """
    )
    @ApiServerErrorResponse
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        List<CategoryDTO> dtos = categoryMapper.toDTOList(categories);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @ApiUpdateCategoryOperation
    @ApiSuccessResponseWithExample(
        description = "Category updated successfully",
        exampleName = "Updated Category",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Home & Garden",
            "description": "Home improvement and gardening supplies including tools, furniture, and plants",
            "createdAt": "2025-01-21T12:56:03",
            "updatedAt": "2025-01-21T13:45:03"
        }
        """
    )
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryDTO categoryDTO) {
        Category updated = categoryService.update(id, categoryMapper.toEntity(categoryDTO));
        return ResponseEntity.ok(categoryMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a category",
        description = "Deletes a category by its unique identifier. This operation cannot be undone.",
        parameters = @Parameter(
            name = "id",
            description = "Unique identifier of the category to delete",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
    )
    @ApiSuccessResponse(description = "Category deleted successfully")
    @ApiBadRequestResponse
    @ApiNotFoundResponse
    @ApiConflictResponse(description = "Category cannot be deleted due to existing dependencies")
    @ApiServerErrorResponse
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search categories",
        description = "Search by partial name and/or description (case-insensitive). Supports pagination and sorting."
    )
    public ResponseEntity<Page<CategoryDTO>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Category> result = categorySearchService.search(name, description, pageable);
        Page<CategoryDTO> dtoPage = result.map(categoryMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }
}
