package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for product creation operation.
 * Combines Operation and RequestBody annotations with detailed product examples.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Create a new product",
    description = "Creates a new product in the catalog with the provided details. The product will be associated with an existing category.",
    requestBody = @RequestBody(
        description = "Product details to create",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ProductDTO.class),
            examples = {
                @ExampleObject(
                    name = "Electronics Product",
                    description = "Example of creating an electronics product",
                    value = """
                    {
                        "name": "Samsung Galaxy S24",
                        "description": "Latest flagship smartphone with advanced camera and AI features",
                        "price": 999.99,
                        "stockQuantity": 50,
                        "categoryId": "123e4567-e89b-12d3-a456-426614174000",
                        "sku": "SGS24-128GB-BLK",
                        "brand": "Samsung",
                        "weight": 0.168,
                        "dimensions": "14.7 x 7.1 x 0.79 cm"
                    }
                    """
                ),
                @ExampleObject(
                    name = "Clothing Product",
                    description = "Example of creating a clothing product",
                    value = """
                    {
                        "name": "Premium Cotton T-Shirt",
                        "description": "Comfortable 100% organic cotton t-shirt available in multiple sizes",
                        "price": 29.99,
                        "stockQuantity": 100,
                        "categoryId": "456e7890-e89b-12d3-a456-426614174001",
                        "sku": "PCT-M-BLU",
                        "brand": "EcoWear",
                        "weight": 0.2,
                        "dimensions": "Medium size"
                    }
                    """
                )
            }
        )
    )
)
@ApiCrudResponses
public @interface ApiCreateProductOperation {
}
