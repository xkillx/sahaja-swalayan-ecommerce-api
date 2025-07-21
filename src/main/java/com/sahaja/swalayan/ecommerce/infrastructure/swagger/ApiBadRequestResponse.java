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
 * Custom composed annotation for bad request responses (400 Bad Request).
 * Commonly used for validation errors and invalid input data.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Invalid input data or validation errors",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        examples = @ExampleObject(
            name = "Validation Error",
            value = """
            {
                "success": false,
                "message": "Validation failed",
                "data": null,
                "timestamp": "2025-01-21T12:56:03"
            }
            """
        )
    )
)
public @interface ApiBadRequestResponse {
    
    /**
     * Custom description for the bad request response.
     * @return the response description
     */
    String description() default "Invalid input data or validation errors";
    
    /**
     * Custom example for the bad request response.
     * @return the example JSON response
     */
    String example() default """
        {
            "success": false,
            "message": "Validation failed",
            "data": null,
            "timestamp": "2025-01-21T12:56:03"
        }
        """;
}
