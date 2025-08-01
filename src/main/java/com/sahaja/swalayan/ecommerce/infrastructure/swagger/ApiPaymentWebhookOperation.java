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
    summary = "Handle payment webhook",
    description = "Process payment status updates from Xendit",
    tags = {"Payments"}
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Webhook processed successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Webhook Success",
                    value = """
                    {
                        "success": true,
                        "message": "Payment webhook processed successfully",
                        "timestamp": "2025-08-01T05:30:45"
                    }
                    """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid webhook payload",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(
                    name = "Invalid Payload",
                    value = """
                    {
                        "success": false,
                        "message": "Invalid webhook payload: missing required fields",
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
                        "message": "An unexpected error occurred processing the webhook",
                        "timestamp": "2025-08-01T05:30:45"
                    }
                    """
                )
            }
        )
    )
})
public @interface ApiPaymentWebhookOperation {
}
