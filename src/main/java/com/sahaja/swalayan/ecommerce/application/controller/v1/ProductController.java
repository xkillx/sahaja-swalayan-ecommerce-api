package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.ProductMapper;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;
import com.sahaja.swalayan.ecommerce.domain.service.ProductService;
import com.sahaja.swalayan.ecommerce.domain.service.FileStorageService;
import com.sahaja.swalayan.ecommerce.common.CategoryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/v1/products")
@ApiProductController
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper,
            CategoryService categoryService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    @ApiGetAllProductsOperation
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.findAll();
        List<ProductDTO> productDTOs = productMapper.toDtoList(products);
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
        Product product = productMapper.toEntity(productDTO);
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
    @ApiSearchProductOperation
    public ResponseEntity<ProductDTO> getProductByName(@RequestParam String name) {
        Product product = productService.findByName(name);
        ProductDTO productDTO = productMapper.toDto(product);
        return ResponseEntity.ok(productDTO);
    }
}
