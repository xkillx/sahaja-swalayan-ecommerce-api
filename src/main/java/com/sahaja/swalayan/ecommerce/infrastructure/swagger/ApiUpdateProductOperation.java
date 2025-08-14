package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import com.sahaja.swalayan.ecommerce.application.dto.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Custom composed annotation for product update operation.
 * Combines Operation, Parameter, and RequestBody annotations with detailed examples.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Update an existing product",
    description = "Updates an existing product with the provided details. Provide a full product payload consistent with validations (required: name, price, quantity, weight).",
    parameters = @Parameter(
        name = "id",
        description = "Unique identifier of the product to update",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    ),
    requestBody = @RequestBody(
        description = "Updated product details",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ProductDTO.class),
            examples = @ExampleObject(
                name = "Product Update",
                description = "Example of updating product details",
                value = """
                {
                    "name": "Samsung Galaxy S24 Ultra",
                    "description": "Updated flagship smartphone with enhanced features and larger display",
                    "price": 1199.99,
                    "quantity": 75,
                    "category_id": "123e4567-e89b-12d3-a456-426614174000",
                    "sku": "SGS24U-256GB-BLK",
                    "weight": 233,
                    "height": 163,
                    "length": 79,
                    "width": 9
                }
                """
            )
        )
    )
)
@ApiCrudResponses
public @interface ApiUpdateProductOperation {
}
