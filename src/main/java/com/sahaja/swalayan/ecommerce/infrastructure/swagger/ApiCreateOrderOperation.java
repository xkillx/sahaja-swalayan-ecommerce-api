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
    summary = "Create a new order from the user's cart",
    description = "Creates a new order for the authenticated user using the items currently in their shopping cart. The order will include all cart items, shipping address, and selected payment method. Returns the created order details including order items, totals, and status."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Order created successfully",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "409", description = "Conflict",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @Content(schema = @Schema(implementation = com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.class)))
})
public @interface ApiCreateOrderOperation {
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "Create Order from Cart";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "Creates a new order from the authenticated user's cart.";
}
