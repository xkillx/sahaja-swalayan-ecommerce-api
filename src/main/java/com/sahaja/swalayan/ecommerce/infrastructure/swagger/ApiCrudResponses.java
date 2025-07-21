package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation for CRUD operation responses.
 * Includes: 200 Success, 400 Bad Request, 401 Unauthorized, 404 Not Found, and 500 Internal Server Error.
 * Use this for endpoints that require authentication and may deal with resource lookups.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {})
@ApiSuccessResponse
@ApiBadRequestResponse
@ApiUnauthorizedResponse
@ApiNotFoundResponse
@ApiServerErrorResponse
public @interface ApiCrudResponses {
}
