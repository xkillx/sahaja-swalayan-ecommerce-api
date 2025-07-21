package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.ProductMapper;
import com.sahaja.swalayan.ecommerce.domain.model.product.Category;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.service.CategoryService;
import com.sahaja.swalayan.ecommerce.domain.service.ProductService;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @Operation(
        summary = "Get all products",
        description = "Retrieves a paginated list of all products in the catalog. Each product includes basic information and category details."
    )
    @ApiSuccessResponseWithExample(
        description = "Products retrieved successfully",
        exampleName = "Product List",
        example = """
        [
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Samsung Galaxy S24",
                "description": "Latest flagship smartphone",
                "price": 999.99,
                "stockQuantity": 50,
                "categoryId": "456e7890-e89b-12d3-a456-426614174001",
                "sku": "SGS24-128GB-BLK",
                "brand": "Samsung",
                "weight": 0.168,
                "dimensions": "14.7 x 7.1 x 0.79 cm"
            }
        ]
        """
    )
    @ApiServerErrorResponse
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.findAll();
        List<ProductDTO> productDTOs = productMapper.toDtoList(products);
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping(value = "/{id}")
    @ApiGetProductOperation
    @ApiSuccessResponseWithExample(
        description = "Product found successfully",
        exampleName = "Product Details",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Samsung Galaxy S24",
            "description": "Latest flagship smartphone with advanced camera and AI features",
            "price": 999.99,
            "stockQuantity": 50,
            "categoryId": "456e7890-e89b-12d3-a456-426614174001",
            "sku": "SGS24-128GB-BLK",
            "brand": "Samsung",
            "weight": 0.168,
            "dimensions": "14.7 x 7.1 x 0.79 cm",
            "createdAt": "2025-01-21T12:56:03",
            "updatedAt": "2025-01-21T12:56:03"
        }
        """
    )
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        Product product = productService.findById(id);
        ProductDTO productDTO = productMapper.toDto(product);
        return ResponseEntity.ok(productDTO);
    }

    @PostMapping
    @ApiCreateProductOperation
    @ApiSuccessResponseWithExample(
        description = "Product created successfully",
        exampleName = "Created Product",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Samsung Galaxy S24",
            "description": "Latest flagship smartphone with advanced camera and AI features",
            "price": 999.99,
            "stockQuantity": 50,
            "categoryId": "456e7890-e89b-12d3-a456-426614174001",
            "sku": "SGS24-128GB-BLK",
            "brand": "Samsung",
            "weight": 0.168,
            "dimensions": "14.7 x 7.1 x 0.79 cm",
            "createdAt": "2025-01-21T12:56:03",
            "updatedAt": "2025-01-21T12:56:03"
        }
        """
    )
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        Category category = categoryService.findById(productDTO.getCategoryId());
        product.setCategory(category);
        Product savedProduct = productService.save(product);
        ProductDTO savedProductDTO = productMapper.toDto(savedProduct);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    @ApiUpdateProductOperation
    @ApiSuccessResponseWithExample(
        description = "Product updated successfully",
        exampleName = "Updated Product",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Samsung Galaxy S24 Ultra",
            "description": "Updated flagship smartphone with enhanced features and larger display",
            "price": 1199.99,
            "stockQuantity": 75,
            "categoryId": "456e7890-e89b-12d3-a456-426614174001",
            "sku": "SGS24U-256GB-BLK",
            "brand": "Samsung",
            "weight": 0.233,
            "dimensions": "16.3 x 7.9 x 0.89 cm",
            "createdAt": "2025-01-21T12:56:03",
            "updatedAt": "2025-01-21T13:45:03"
        }
        """
    )
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

    @GetMapping(value = "/search")
    @ApiSearchProductOperation
    @ApiSuccessResponseWithExample(
        description = "Product found successfully",
        exampleName = "Search Result",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Samsung Galaxy S24",
            "description": "Latest flagship smartphone with advanced camera and AI features",
            "price": 999.99,
            "stockQuantity": 50,
            "categoryId": "456e7890-e89b-12d3-a456-426614174001",
            "sku": "SGS24-128GB-BLK",
            "brand": "Samsung",
            "weight": 0.168,
            "dimensions": "14.7 x 7.1 x 0.79 cm"
        }
        """
    )
    public ResponseEntity<ProductDTO> getProductByName(@RequestParam String name) {
        Product product = productService.findByName(name);
        ProductDTO productDTO = productMapper.toDto(product);
        return ResponseEntity.ok(productDTO);
    }
}
