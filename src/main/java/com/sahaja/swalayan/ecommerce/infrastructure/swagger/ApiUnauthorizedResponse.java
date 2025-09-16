package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for unauthorized responses (401 Unauthorized).
 * Used when authentication is required but not provided or invalid.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "401",
    description = "Authentication required or invalid credentials",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        examples = @ExampleObject(
            name = "Unauthorized Error",
            value = """
            {
                "success": false,
                "message": "Authentication required",
                "data": null,
                "timestamp": "2025-01-21T12:56:03"
            }
            """
        )
    )
)
public @interface ApiUnauthorizedResponse {
    
    /**
     * Custom description for the unauthorized response.
     * @return the response description
     */
    @AliasFor(annotation = ApiResponse.class, attribute = "description")
    String description() default "Authentication required or invalid credentials";
}
