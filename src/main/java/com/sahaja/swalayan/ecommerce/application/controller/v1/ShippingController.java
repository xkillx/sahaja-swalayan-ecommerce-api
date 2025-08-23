package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/shipping")
public class ShippingController {

    private final ShippingService shippingService;
    private final com.sahaja.swalayan.ecommerce.application.service.ShippingWebhookService shippingWebhookService;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${shipping.webhook.token:}")
    private String shippingWebhookToken;

    // Temporary open flag to allow easy local testing; default true in application.yaml for dev
    @org.springframework.beans.factory.annotation.Value("${shipping.webhook.open:false}")
    private boolean shippingWebhookOpen;

    // Biteship signature verification configs
    @org.springframework.beans.factory.annotation.Value("${shipping.webhook.signature.key:}")
    private String signatureHeaderKey;

    @org.springframework.beans.factory.annotation.Value("${shipping.webhook.signature.secret:}")
    private String signatureSecret;

    @Operation(summary = "Get available couriers for selection during checkout")
    @GetMapping("/couriers")
    public ResponseEntity<CourierResponseDTO> getAvailableCouriers() {
        log.debug("Fetching available couriers");
        CourierResponseDTO response = shippingService.getAvailableCouriers();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search areas for address selection", description = "Searches areas (cities, districts) to help users pick accurate area_id for shipping.")
    @GetMapping("/areas")
    public ResponseEntity<com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.AreaResponseDTO> searchAreas(
            @RequestParam(name = "q") String query) {
        log.debug("Searching areas: {}", query);
        var response = shippingService.searchAreas(query);
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
            @RequestHeader(required = false) Map<String, String> headers,
            @RequestBody(required = false) String rawBody
    ) {
        // --- 0) Open/testing mode: bypass signature ---
        if (shippingWebhookOpen) {
            try {
                if (rawBody != null && !rawBody.isBlank()) {
                    ShippingWebhookPayload payload = objectMapper.readValue(rawBody, ShippingWebhookPayload.class);
                    shippingWebhookService.handleWebhook(payload);
                } else {
                    log.debug("[shipping-webhook] Open mode: empty body accepted");
                }
            } catch (Exception e) {
                log.warn("[shipping-webhook] Open mode: error processing payload: {}", e.getMessage());
            }
            return ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("OK"));
        }

        // --- 1) Resolve config ---
        final String headerKey = firstNonBlank(this.signatureHeaderKey,
                System.getenv("SHIPPING_WEBHOOK_SIGNATURE_KEY"),
                System.getProperty("shipping.webhook.signature.key"),
                "X-Biteship-Signature"); // sane default

        final String secret = firstNonBlank(this.signatureSecret,
                System.getenv("SHIPPING_WEBHOOK_SIGNATURE_SECRET"),
                System.getProperty("shipping.webhook.signature.secret"));

        if (!hasText(headerKey) || !hasText(secret)) {
            log.warn("[shipping-webhook] Signature verification not configured (missing key/secret)");
            return ResponseEntity.status(500)
                    .body(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.error("Signature verification not configured"));
        }

        // --- 2) Read provided signature (case-insensitive header) ---
        final String providedSignature = getHeaderIgnoreCase(headers, headerKey);
        if (!hasText(providedSignature)) {
            log.warn("[shipping-webhook] Missing signature header: {}", headerKey);
            return ResponseEntity.status(403)
                    .body(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.error("Missing signature header"));
        }

        if (rawBody == null) rawBody = "";

        // --- 3) Validate signature (shared-secret OR HMAC-SHA256) ---
        try {
            // Mode A: shared-secret header value equals our secret
            boolean sharedSecretOk = constantTimeEquals(providedSignature, secret);

            // Mode B: HMAC over raw body (support Base64 or hex header)
            byte[] mac = hmacSha256(rawBody, secret);
            String computedBase64 = Base64.getEncoder().encodeToString(mac);
            String computedHexLower = toHexLower(mac);

            boolean hmacOk =
                    constantTimeEquals(providedSignature, computedBase64) ||
                            constantTimeEquals(providedSignature, computedHexLower);

            if (!(sharedSecretOk || hmacOk)) {
                log.warn("[shipping-webhook] Invalid signature provided");
                return ResponseEntity.status(403)
                        .body(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.error("Invalid signature"));
            }
        } catch (Exception ex) {
            log.error("[shipping-webhook] Error computing signature: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.error("Signature verification failed"));
        }

        // --- 4) Process payload (don’t trigger retry storms) ---
        try {
            if (rawBody != null && !rawBody.isBlank()) {
                ShippingWebhookPayload payload = objectMapper.readValue(rawBody, ShippingWebhookPayload.class);
                shippingWebhookService.handleWebhook(payload);
            } else {
                log.warn("[shipping-webhook] Valid signature but empty body");
            }
            return ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("Shipping webhook processed successfully"));
        } catch (Exception ex) {
            log.warn("[shipping-webhook] Valid signature but error parsing payload: {}", ex.getMessage());
            return ResponseEntity.ok(com.sahaja.swalayan.ecommerce.application.dto.ApiResponse.success("OK"));
        }
    }

    /* ===================== helpers ===================== */

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String getHeaderIgnoreCase(Map<String, String> headers, String key) {
        if (headers == null || key == null) return null;
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) return e.getValue();
        }
        return null;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(x, y); // constant-time
    }

    private static byte[] hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC init/final failed", e);
        }
    }

    private static String toHexLower(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte by : bytes) sb.append(String.format("%02x", by));
        return sb.toString();
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) if (v != null && !v.trim().isEmpty()) return v.trim();
        return null;
    }

}
