package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.ProductMapper;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;
import com.sahaja.swalayan.ecommerce.domain.service.ProductService;
import com.sahaja.swalayan.ecommerce.domain.service.ProductSearchService;
import com.sahaja.swalayan.ecommerce.domain.service.FileStorageService;
import com.sahaja.swalayan.ecommerce.common.CategoryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/v1/products")
@ApiProductController
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final ProductSearchService productSearchService;

    @GetMapping
    @ApiGetAllProductsOperation
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String categoryParam,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // Determine keyword from either q or name
        String keyword = (q != null && !q.isBlank()) ? q : (name != null && !name.isBlank() ? name : null);

        // Resolve category: accept UUID string or category name
        UUID categoryId = null;
        if (categoryParam != null && !categoryParam.isBlank()) {
            try {
                categoryId = UUID.fromString(categoryParam.trim());
            } catch (IllegalArgumentException ex) {
                // Not a UUID: try resolve by name
                try {
                    Category cat = categoryService.findByName(categoryParam.trim());
                    if (cat != null) {
                        categoryId = cat.getId();
                    }
                } catch (Exception ignored) {
                    // If not found by name, keep categoryId null (no filter)
                }
            }
        }

        // Use search service for unified handling of filters + sorting + pagination
        Page<Product> page = productSearchService.search(
                keyword,
                categoryId,
                null, // minPrice
                null, // maxPrice
                null, // available
                pageable
        );
        List<ProductDTO> productDTOs = productMapper.toDtoList(page.getContent());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping(value = "/{id}")
    @ApiGetProductOperation
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        Product product = productService.findById(id);
        ProductDTO productDTO = productMapper.toDto(product);
        return ResponseEntity.ok(productDTO);
    }

    @PostMapping
    @ApiCreateProductOperation
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        UUID categoryId = productDTO.getCategoryId();
        Category category;
        if (categoryId == null) {
            // Assign default 'Uncategorised' category
            category = categoryService.findById(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        } else {
            try {
                category = categoryService.findById(categoryId);
            } catch (CategoryNotFoundException ex) {
                log.error("Product creation failed: categoryId {} not found", categoryId);
                throw new CategoryNotFoundException("Invalid categoryId supplied: " + categoryId);
            }
        }
        product.setCategory(category);
        Product savedProduct = productService.save(product);
        ProductDTO savedProductDTO = productMapper.toDto(savedProduct);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    @ApiUpdateProductOperation
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductDTO productDTO) {
        // Map incoming DTO to entity
        Product product = productMapper.toEntity(productDTO);

        // Resolve category to avoid null category_id constraint violations
        UUID categoryId = productDTO.getCategoryId();
        Category category;
        if (categoryId == null) {
            // Keep existing category if present; otherwise default to 'Uncategorised'
            Product existing = productService.findById(id);
            if (existing.getCategory() != null) {
                category = existing.getCategory();
            } else {
                category = categoryService.findById(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            }
        } else {
            try {
                category = categoryService.findById(categoryId);
            } catch (CategoryNotFoundException ex) {
                log.error("Product update failed: categoryId {} not found", categoryId);
                throw new CategoryNotFoundException("Invalid categoryId supplied: " + categoryId);
            }
        }
        product.setCategory(category);

        Product updatedProduct = productService.update(id, product);
        ProductDTO updatedProductDTO = productMapper.toDto(updatedProduct);
        return ResponseEntity.ok(updatedProductDTO);
    }

    @DeleteMapping(value = "/{id}")
    @ApiDeleteProductOperation
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    @ApiUploadProductImageOperation
    public ResponseEntity<ProductDTO> uploadProductImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = fileStorageService.storeProductImage(file, id.toString());
        Product updated = productService.updateProductImage(id, imageUrl);
        ProductDTO dto = productMapper.toDto(updated);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/search")
    @Operation(
        summary = "Search products",
        description = "Keyword search on name, description, and sku with optional filters for category (UUID), price range, and availability. Supports pagination and sorting."
    )
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Product> result = productSearchService.search(keyword, categoryId, minPrice, maxPrice, available, pageable);
        return ResponseEntity.ok(result);
    }
}
