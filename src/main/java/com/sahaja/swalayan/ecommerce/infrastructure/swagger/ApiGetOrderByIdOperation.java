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
    summary = "Get order by ID for authenticated user",
    description = "Retrieves the details of a specific order by its unique identifier for the authenticated user. The response includes all order items, shipping information, payment method, status, and timestamps. Useful for viewing order history or tracking a specific order."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Order fetched successfully",
        content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    @ApiResponse(responseCode = "404", description = "Order not found",
        content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = ApiResponse.class)))
})
public @interface ApiGetOrderByIdOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Get Order by ID";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "Retrieves a specific order by its ID for the authenticated user.";
}
