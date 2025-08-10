package com.sahaja.swalayan.ecommerce.application.controller.v1;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/shipping")
public class ShippingController {

    private final ShippingService shippingService;

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
}
