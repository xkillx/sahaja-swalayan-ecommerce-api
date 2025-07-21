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
 * Custom composed annotation for conflict responses (409 Conflict).
 * Commonly used when a resource already exists (e.g., email already registered).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "409",
    description = "Resource conflict - resource already exists",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        examples = @ExampleObject(
            name = "Conflict Error",
            value = """
            {
                "success": false,
                "message": "Email already registered",
                "data": null,
                "timestamp": "2025-01-21T12:56:03"
            }
            """
        )
    )
)
public @interface ApiConflictResponse {
    
    /**
     * Custom description for the conflict response.
     * @return the response description
     */
    String description() default "Resource conflict - resource already exists";
    
    /**
     * Custom example for the conflict response.
     * @return the example JSON response
     */
    String example() default """
        {
            "success": false,
            "message": "Email already registered",
            "data": null,
            "timestamp": "2025-01-21T12:56:03"
        }
        """;
}
