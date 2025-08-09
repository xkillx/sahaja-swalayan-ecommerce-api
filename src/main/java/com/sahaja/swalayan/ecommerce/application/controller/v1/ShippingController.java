package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.domain.service.ShippingService;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
