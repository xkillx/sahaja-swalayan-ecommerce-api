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
@Operation(
    summary = "Get all orders for authenticated user",
    description = "Retrieves a list of all orders placed by the currently authenticated user. The response includes order summaries, status, order dates, and basic information for each order. Useful for displaying order history in user account sections."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Orders fetched successfully",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class)))
})
public @interface ApiGetUserOrdersOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Get All User Orders";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "Retrieves all orders for the authenticated user.";
}
