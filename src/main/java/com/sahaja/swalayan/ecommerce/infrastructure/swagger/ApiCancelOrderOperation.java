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
    summary = "Cancel order by ID for authenticated user",
    description = "Cancels an existing order by its unique identifier for the authenticated user. Only orders that are still in a cancellable state (e.g., not yet shipped) can be cancelled. The response includes the updated order status and details."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "404", description = "Order not found",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "409", description = "Conflict",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class)))
})
public @interface ApiCancelOrderOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Cancel Order";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "Cancels an existing order belonging to the authenticated user.";
}
