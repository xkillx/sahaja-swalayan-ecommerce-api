package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.PaymentRequest;
import com.sahaja.swalayan.ecommerce.application.dto.PaymentResponse;
import com.sahaja.swalayan.ecommerce.application.dto.XenditWebhookPayload;
import com.sahaja.swalayan.ecommerce.common.InvalidXenditWebhookException;
import com.sahaja.swalayan.ecommerce.common.InvalidXenditPayloadException;
import com.sahaja.swalayan.ecommerce.domain.service.PaymentService;
import com.sahaja.swalayan.ecommerce.infrastructure.config.XenditProperties;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiCreatePaymentOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiGetPaymentOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiGetPaymentsByOrderOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiPaymentWebhookOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@Tag(name = "Payments", description = "Payment processing endpoints")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final XenditProperties xenditProperties;

    public PaymentController(PaymentService paymentService, XenditProperties xenditProperties) {
        this.paymentService = paymentService;
        this.xenditProperties = xenditProperties;
    }

    @PostMapping
    @ApiCreatePaymentOperation
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", payment));
    }

    @GetMapping("/{id}")
    @ApiGetPaymentOperation
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        PaymentResponse payment = paymentService.getPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @GetMapping("/order/{orderId}")
    @ApiGetPaymentsByOrderOperation
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrderId(@PathVariable UUID orderId) {
        List<PaymentResponse> payments = paymentService.getByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payments for order retrieved successfully", payments));
    }

    @PostMapping("/webhook")
    @ApiPaymentWebhookOperation
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestHeader(value = "X-Callback-Token", required = false) String callbackToken,
            @RequestBody XenditWebhookPayload payload) {
        
        // Validate the callback token first
        validateCallbackToken(callbackToken);
        
        // Validate webhook payload
        if (payload == null || payload.getExternalId() == null || payload.getStatus() == null) {
            log.debug("Invalid webhook payload: {}", payload);
            throw new InvalidXenditPayloadException("Invalid webhook payload");
        }
        
        log.debug("Received webhook callback for externalId: {}, status: {}", 
                payload.getExternalId(), payload.getStatus());
        
        // Process the webhook payload
        paymentService.handleXenditWebhook(payload);
        log.debug("Successfully processed webhook for externalId: {}", payload.getExternalId());
        return ResponseEntity.ok(ApiResponse.success("Payment webhook processed successfully"));
    }
    
    /**
     * Validates the Xendit callback token from the webhook request header
     * 
     * @param callbackToken Token from request header
     * @throws InvalidXenditWebhookException if token is invalid or missing
     */
    private void validateCallbackToken(String callbackToken) {
        String expectedToken = xenditProperties.getCallbackToken();
        
        if (!StringUtils.hasText(callbackToken)) {
            log.debug("Missing X-Callback-Token header in webhook request");
            throw new InvalidXenditWebhookException("Missing X-Callback-Token header");
        }
        
        if (!Objects.equals(callbackToken, expectedToken)) {
            log.debug("Invalid X-Callback-Token provided in webhook request");
            throw new InvalidXenditWebhookException("Invalid X-Callback-Token");
        }
        
        log.debug("Xendit callback token validation successful");
    }
}
