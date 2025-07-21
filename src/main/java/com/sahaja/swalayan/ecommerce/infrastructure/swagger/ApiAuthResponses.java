package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation specifically for authentication endpoints.
 * Includes: 200 Success, 400 Bad Request, 409 Conflict, and 500 Internal Server Error.
 * Use this for registration, login, and other auth-related endpoints.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {})
@ApiSuccessResponse
@ApiBadRequestResponse
@ApiConflictResponse
@ApiServerErrorResponse
public @interface ApiAuthResponses {
}
