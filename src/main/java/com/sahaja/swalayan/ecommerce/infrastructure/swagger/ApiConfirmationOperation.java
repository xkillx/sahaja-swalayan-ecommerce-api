package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for email confirmation operation.
 * Combines Operation and Parameter annotations with predefined values.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Confirm user email",
    description = "Confirms a user's email address using the confirmation token sent via email during registration.",
    parameters = @Parameter(
        name = "token",
        description = "Email confirmation token received via email",
        required = true,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
    )
)
@ApiStandardResponses
public @interface ApiConfirmationOperation {
}
