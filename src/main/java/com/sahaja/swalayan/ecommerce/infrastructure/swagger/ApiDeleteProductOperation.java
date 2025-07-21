package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for product deletion operation.
 * Combines Operation and Parameter annotations with detailed descriptions.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Delete a product",
    description = "Permanently deletes a product from the catalog. This operation cannot be undone. Ensure the product is not referenced in any orders before deletion.",
    parameters = @Parameter(
        name = "id",
        description = "Unique identifier of the product to delete",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
)
@ApiSuccessResponse(description = "Product deleted successfully")
@ApiBadRequestResponse
@ApiNotFoundResponse
@ApiConflictResponse(description = "Product cannot be deleted due to existing references")
@ApiServerErrorResponse
public @interface ApiDeleteProductOperation {
}
