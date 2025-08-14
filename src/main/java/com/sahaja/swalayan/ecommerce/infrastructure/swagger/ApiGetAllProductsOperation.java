package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Operation(
    summary = "Get all products",
    description = "Retrieves a paginated list of all products in the catalog. Each product includes basic information and category details."
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Products retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Product List",
                value = "[\n  {\n    \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n    \"name\": \"Samsung Galaxy S24\",\n    \"description\": \"Latest flagship smartphone\",\n    \"price\": 999.99,\n    \"quantity\": 50,\n    \"category_id\": \"456e7890-e89b-12d3-a456-426614174001\",\n    \"sku\": \"SGS24-128GB-BLK\",\n    \"weight\": 168,\n    \"height\": 147,\n    \"length\": 71,\n    \"width\": 8\n  }\n]"
            )
        )
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error"
    )
})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ApiGetAllProductsOperation {}
