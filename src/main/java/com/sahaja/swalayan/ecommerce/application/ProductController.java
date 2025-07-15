package com.sahaja.swalayan.ecommerce.application;

import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
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
@RequestMapping(value = "/products")
@Tag(name = "Product API", description = "Operations related to products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
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
