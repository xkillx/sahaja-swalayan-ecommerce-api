package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for getting a product by ID operation.
 * Combines Operation and Parameter annotations with detailed descriptions.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Get product by ID",
    description = "Retrieves a specific product by its unique identifier. Returns detailed product information including category details.",
    parameters = @Parameter(
        name = "id",
        description = "Unique identifier of the product to retrieve",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
)
@ApiStandardResponses
@ApiNotFoundResponse
public @interface ApiGetProductOperation {
}
