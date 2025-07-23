package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for not found responses (404 Not Found).
 * Used when a requested resource does not exist.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Resource not found",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        examples = @ExampleObject(
            name = "Not Found Error",
            value = """
            {
                "success": false,
                "message": "Resource not found",
                "data": null,
                "timestamp": "2025-01-21T12:56:03"
            }
            """
        )
    )
)
public @interface ApiNotFoundResponse {
}

