package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for successful API responses with customizable examples.
 * Allows dynamic configuration of response description and example content.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "200",
    description = "Operation completed successfully",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class)
    )
)
public @interface ApiSuccessResponseWithExample {
    
    /**
     * Custom description for the success response.
     * @return the response description
     */
    @AliasFor(annotation = ApiResponse.class, attribute = "description")
    String description() default "Operation completed successfully";
    
    /**
     * Name for the example in Swagger UI.
     * @return the example name
     */
    String exampleName() default "Success Response";
    
    /**
     * Example response body for documentation.
     * @return the example JSON response
     */
    String example();
}
