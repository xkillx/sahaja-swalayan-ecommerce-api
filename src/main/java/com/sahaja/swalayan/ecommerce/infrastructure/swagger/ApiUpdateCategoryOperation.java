package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
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
 * Custom composed annotation for category update operation.
 * Combines Operation, Parameter, and RequestBody annotations.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Update an existing category",
    description = "Updates an existing category with the provided details. Category names must remain unique.",
    parameters = @Parameter(
        name = "id",
        description = "Unique identifier of the category to update",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    ),
    requestBody = @RequestBody(
        description = "Updated category details",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CategoryDTO.class),
            examples = @ExampleObject(
                name = "Category Update Example",
                value = """
                {
                    "name": "Home & Garden",
                    "description": "Home improvement and gardening supplies including tools, furniture, and plants"
                }
                """
            )
        )
    )
)
@ApiStandardResponses
@ApiNotFoundResponse
@ApiConflictResponse(description = "Category name already exists")
public @interface ApiUpdateCategoryOperation {
}
