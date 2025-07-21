package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
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
 * Custom composed annotation for user registration operation.
 * Combines Operation and RequestBody annotations with predefined values.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Register a new user",
    description = "Creates a new user account and sends a confirmation email. The user must confirm their email before they can log in.",
    requestBody = @RequestBody(
        description = "User registration details",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = RegisterRequest.class),
            examples = @ExampleObject(
                name = "Registration Example",
                value = """
                {
                    "name": "John Doe",
                    "email": "john.doe@example.com",
                    "password": "SecurePassword123",
                    "phone": "+6281234567890"
                }
                """
            )
        )
    )
)
@ApiAuthResponses
public @interface ApiRegistrationOperation {
}
