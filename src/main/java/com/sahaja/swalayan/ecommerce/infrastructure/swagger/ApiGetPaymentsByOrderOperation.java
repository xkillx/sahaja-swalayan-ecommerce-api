package com.sahaja.swalayan.ecommerce.infrastructure.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
    summary = "Get payments by order ID",
    description = "Retrieve all payments associated with an order",
    tags = {"Payments"}
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Payments for order retrieved successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Order Payments",
                    value = """
                    {
                        "success": true,
                        "message": "Payments for order retrieved successfully",
                        "data": [
                            {
                                "paymentId": "123e4567-e89b-12d3-a456-426614174000",
                                "paymentStatus": "PAID",
                                "xenditInvoiceUrl": "https://checkout.xendit.co/web/123456789"
                            }
                        ],
                        "timestamp": "2025-08-01T05:30:45"
                    }
                    """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Order not found or has no payments",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Not Found Error",
                    value = """
                    {
                        "success": false,
                        "message": "No payments found for order with ID 123e4567-e89b-12d3-a456-426614174000",
                        "timestamp": "2025-08-01T05:30:45"
                    }
                    """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Server Error",
                    value = """
                    {
                        "success": false,
                        "message": "An unexpected error occurred",
                        "timestamp": "2025-08-01T05:30:45"
                    }
                    """
                )
            }
        )
    )
})
public @interface ApiGetPaymentsByOrderOperation {
}
