package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for product search operation.
 * Combines Operation and Parameter annotations for product search functionality.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Search for a product by name",
    description = "Finds a product by its exact name. The search is case-sensitive and returns the first matching product.",
    parameters = @Parameter(
        name = "name",
        description = "Exact name of the product to search for",
        required = true,
        example = "Samsung Galaxy S24"
    )
)
@ApiStandardResponses
@ApiNotFoundResponse
public @interface ApiSearchProductOperation {
}
