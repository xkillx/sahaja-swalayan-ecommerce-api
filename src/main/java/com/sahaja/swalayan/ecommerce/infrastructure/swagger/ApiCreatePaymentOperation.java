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
    summary = "Create a new payment",
    description = "Create a payment for an order with specified payment method",
    tags = {"Payments"}
)
@ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Payment created successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Payment Success",
                    value = """
                    {
                        "success": true,
                        "message": "Payment created successfully",
                        "data": {
                            "paymentId": "123e4567-e89b-12d3-a456-426614174000",
                            "paymentStatus": "PENDING",
                            "xenditInvoiceUrl": "https://checkout.xendit.co/web/123456789"
                        },
                        "timestamp": "2025-08-01T05:30:45"
                    }
                    """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request - Invalid payment data",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                        "success": false,
                        "message": "Validation failed",
                        "errors": [
                            "Amount must be greater than zero",
                            "Order ID cannot be null"
                        ],
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
public @interface ApiCreatePaymentOperation {
}
