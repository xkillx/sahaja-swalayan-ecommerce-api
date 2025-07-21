package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
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
 * Custom composed annotation for category creation operation.
 * Combines Operation and RequestBody annotations with category examples.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Create a new category",
    description = "Creates a new product category with the provided details. Category names must be unique.",
    requestBody = @RequestBody(
        description = "Category details to create",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CategoryDTO.class),
            examples = @ExampleObject(
                name = "Category Creation Example",
                value = """
                {
                    "name": "Electronics",
                    "description": "Electronic devices and accessories including smartphones, laptops, and gadgets"
                }
                """
            )
        )
    )
)
@ApiStandardResponses
@ApiConflictResponse(description = "Category with this name already exists")
public @interface ApiCreateCategoryOperation {
}
