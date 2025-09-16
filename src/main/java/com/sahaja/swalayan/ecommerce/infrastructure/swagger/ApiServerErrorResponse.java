package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for internal server error responses (500 Internal Server Error).
 * Used for unexpected system errors.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "500",
    description = "Internal server error",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        examples = @ExampleObject(
            name = "Server Error",
            value = """
            {
                "success": false,
                "message": "An unexpected error occurred",
                "data": null,
                "timestamp": "2025-01-21T12:56:03"
            }
            """
        )
    )
)
public @interface ApiServerErrorResponse {
    
    /**
     * Custom description for the server error response.
     * @return the response description
     */
    @AliasFor(annotation = ApiResponse.class, attribute = "description")
    String description() default "Internal server error";
    
    /**
     * Custom example for the server error response.
     * @return the example JSON response
     */
    String example() default """
        {
            "success": false,
            "message": "An unexpected error occurred",
            "data": null,
            "timestamp": "2025-01-21T12:56:03"
        }
        """;
}
