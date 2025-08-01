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
    summary = "Get payment details",
    description = "Retrieve payment details by payment ID",
    tags = {"Payments"}
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Payment details retrieved successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Payment Details",
                    value = """
                    {
                        "success": true,
                        "message": "Payment retrieved successfully",
                        "data": {
                            "paymentId": "123e4567-e89b-12d3-a456-426614174000",
                            "paymentStatus": "PAID",
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
        responseCode = "404",
        description = "Payment not found",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Not Found Error",
                    value = """
                    {
                        "success": false,
                        "message": "Payment with ID 123e4567-e89b-12d3-a456-426614174000 not found",
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
public @interface ApiGetPaymentOperation {
}
