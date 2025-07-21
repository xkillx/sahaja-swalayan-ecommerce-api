package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom composed annotation that combines the most common API response patterns.
 * Includes: 200 Success, 400 Bad Request, and 500 Internal Server Error.
 * Use this for endpoints that follow standard CRUD patterns.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {})
@ApiSuccessResponse
@ApiBadRequestResponse
@ApiServerErrorResponse
public @interface ApiStandardResponses {
}
