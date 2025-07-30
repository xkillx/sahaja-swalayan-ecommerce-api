package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(summary = "Order operation", description = "Order management operation")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", 
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", 
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", 
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "409", description = "Conflict", 
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error", 
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class)))
})
/**
 * Standard composed Swagger annotation for Order endpoints.
 * Use this for generic order operations if endpoint-specific annotation is not needed.
 */
public @interface ApiOrderOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Order operation";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "Order management operation";
}
