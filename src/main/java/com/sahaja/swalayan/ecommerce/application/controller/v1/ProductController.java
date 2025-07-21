package com.sahaja.swalayan.ecommerce.application.controller;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;
import com.sahaja.swalayan.ecommerce.domain.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.ProductMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/v1/products")
@Tag(name = "Product API", description = "Operations related to products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper, CategoryService categoryService) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.findAll();
        List<ProductDTO> productDTOs = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        Product product = productService.findById(id);
        ProductDTO productDTO = productMapper.toDto(product);
        return ResponseEntity.ok(productDTO);
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        Category category = categoryService.findById(productDTO.getCategoryId());
        product.setCategory(category);
        Product savedProduct = productService.save(product);
        ProductDTO savedProductDTO = productMapper.toDto(savedProduct);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        Product updatedProduct = productService.update(id, product);
        ProductDTO updatedProductDTO = productMapper.toDto(updatedProduct);
        return ResponseEntity.ok(updatedProductDTO);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/search")
    @Operation(summary = "Search for a product by name")
    public ResponseEntity<ProductDTO> getProductByName(@RequestParam String name) {
        Product product = productService.findByName(name);
        ProductDTO productDTO = productMapper.toDto(product);
        return ResponseEntity.ok(productDTO);
    }
}
