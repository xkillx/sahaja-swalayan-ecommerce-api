package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ShippingWebhookPayload;
import com.sahaja.swalayan.ecommerce.domain.service.ShippingService;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.TrackingResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancellationReasonResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancelOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancelOrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/shipping")
public class ShippingController {

    private final ShippingService shippingService;
    private final com.sahaja.swalayan.ecommerce.application.service.ShippingWebhookService shippingWebhookService;

    @org.springframework.beans.factory.annotation.Value("${shipping.webhook.token:}")
    private String shippingWebhookToken;

    // Temporary open flag to allow easy local testing; default true in application.yaml for dev
    @org.springframework.beans.factory.annotation.Value("${shipping.webhook.open:false}")
    private boolean shippingWebhookOpen;

    @Operation(summary = "Get available couriers for selection during checkout")
    @GetMapping("/couriers")
    public ResponseEntity<CourierResponseDTO> getAvailableCouriers() {
        log.debug("Fetching available couriers");
        CourierResponseDTO response = shippingService.getAvailableCouriers();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List cancellation reasons supported by courier")
    @GetMapping("/cancel-reasons")
    public ResponseEntity<CancellationReasonResponseDTO> getCancellationReasons(
            @RequestParam(name = "lang", defaultValue = "en") String lang) {
        log.debug("Fetching cancellation reasons, lang: {}", lang);
        CancellationReasonResponseDTO response = shippingService.getCancellationReasons(lang);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calculate shipping rates given origin, destination, weight, and courier")
    @PostMapping("/rates")
    public ResponseEntity<CourierRateResponseDTO> calculateRates(@RequestBody CourierRateRequestDTO request) {
        log.debug("Calculating shipping rates: {}", request);
        CourierRateResponseDTO response = shippingService.getCourierRates(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a shipping order (usually after payment confirmation)")
    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponseDTO> createOrder(@RequestBody CreateOrderRequestDTO request) {
        log.debug("Creating shipping order: {}", request);
        CreateOrderResponseDTO response = shippingService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel a shipment (if courier allows)")
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<CancelOrderResponseDTO> cancelOrder(
            @PathVariable String orderId,
            @RequestBody CancelOrderRequestDTO request) {
        log.debug("Cancelling shipping order: {} with payload: {}", orderId, request);
        CancelOrderResponseDTO response = shippingService.cancelOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get live tracking updates from courier API")
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<TrackingResponseDTO> getOrderTracking(@PathVariable String trackingId) {
        log.debug("Retrieving live tracking updates: {}", trackingId);
        TrackingResponseDTO response = shippingService.getTrackingById(trackingId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve public tracking by waybill and courier code")
    @GetMapping("/trackings/{waybillId}/couriers/{courierCode}")
    public ResponseEntity<TrackingResponseDTO> getPublicTracking(
            @PathVariable String waybillId,
            @PathVariable String courierCode) {
        log.debug("Retrieving public tracking for waybillId: {}, courierCode: {}", waybillId, courierCode);
        TrackingResponseDTO response = shippingService.getPublicTracking(waybillId, courierCode);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Shipping provider webhook callback (e.g., Biteship)")
    @PostMapping(value = "/webhook", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<com.sahaja.swalayan.ecommerce.application.dto.ApiResponse<Void>> handleShippingWebhook(
            @RequestHeader(value = "X-Callback-Token", required = false) String callbackToken,
            @RequestBody(required = false) ShippingWebhookPayload payload
    ) {
        // If open flag is enabled, bypass token validation and accept empty body, returning OK
        if (shippingWebhookOpen) {
            try {
                if (payload != null) {
                    shippingWebhookService.handleWebhook(payload);
                } else {
                    log.debug("[shipping-webhook] Open mode: empty body accepted");
                }
            } catch (Exception e) {
                log.warn("[shipping-webhook] Open mode: error processing payload: {}", e.getMessage());
            }
            return ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("OK"));
        }

        // Optional token validation via config property shipping.webhook.token with fallback to env/JVM
        String expected = this.shippingWebhookToken;
        if (expected == null || expected.isBlank()) {
            expected = System.getenv("SHIPPING_WEBHOOK_TOKEN");
        }
        if (expected == null || expected.isBlank()) {
            expected = System.getProperty("shipping.webhook.token");
        }
        if (expected != null && !expected.isBlank()) {
            if (callbackToken == null || !expected.equals(callbackToken)) {
                log.warn("Invalid or missing X-Callback-Token for shipping webhook");
                return ResponseEntity.status(403)
                        .body(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.error("Invalid X-Callback-Token"));
            }
        }
        shippingWebhookService.handleWebhook(payload);
        return ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("Shipping webhook processed successfully"));
    }
}
