package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.service.ShippingService;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.BiteshipShippingClient;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.AreaResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRateResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CreateOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.RetrieveOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancellationReasonResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancelOrderRequestDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CancelOrderResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.TrackingResponseDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.PricingDTO;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.CourierRetrieveDTO;
import com.sahaja.swalayan.ecommerce.common.ShippingException;
import com.sahaja.swalayan.ecommerce.infrastructure.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class ShippingServiceImpl implements ShippingService {

    private final BiteshipShippingClient biteshipShippingClient;

    @org.springframework.beans.factory.annotation.Value("${shipping.stub.enabled:false}")
    private boolean shippingStubEnabled;

    @Autowired
    public ShippingServiceImpl(BiteshipShippingClient biteshipShippingClient) {
        this.biteshipShippingClient = biteshipShippingClient;
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_AREAS, key = "#input != null ? #input.trim().toLowerCase() : 'null'", unless = "#result == null")
    public AreaResponseDTO searchAreas(String input) {
        log.debug("Starting searchAreas with input: {}", input);
        try {
            if (input == null || input.isBlank()) {
                log.debug("Invalid area search input: {}", input);
                throw new ShippingException("Search input must not be null or blank");
            }
            AreaResponseDTO response = biteshipShippingClient.searchAreas(input);
            log.debug("Successfully searched areas for input: {}", input);
            return response;
        } catch (Exception e) {
            log.error("Failed to search areas for input: {}. Error: {}", input, e.getMessage(), e);
            throw new ShippingException("Failed to search areas: " + e.getMessage(), e);
        }
    }

    private boolean stubEnabled() {
        try {
            if (shippingStubEnabled) return true; // from application.yaml
            String flag = System.getenv("SHIPPING_STUB");
            if (flag == null) flag = System.getProperty("shipping.stub.enabled");
            return flag != null && flag.trim().equalsIgnoreCase("true");
        } catch (Exception ignore) {
            return false;
        }
    }

    private CourierResponseDTO buildStubCouriers() {
        List<CourierRetrieveDTO> list = new ArrayList<>();
        list.add(CourierRetrieveDTO.builder()
                .courierName("Gojek")
                .courierCode("gojek")
                .courierServiceName("Instant")
                .courierServiceCode("instant")
                .serviceType("same_day")
                .shippingType("parcel")
                .description("On Demand Instant (bike)")
                .shipmentDurationRange("1 - 3")
                .shipmentDurationUnit("hours")
                .availableForInstantWaybillId(true)
                .build());
        list.add(CourierRetrieveDTO.builder()
                .courierName("Gojek")
                .courierCode("gojek")
                .courierServiceName("Same Day")
                .courierServiceCode("same_day")
                .serviceType("same_day")
                .shippingType("parcel")
                .description("On Demand within 8 hours (bike)")
                .shipmentDurationRange("6 - 8")
                .shipmentDurationUnit("hours")
                .availableForInstantWaybillId(true)
                .build());
        list.add(CourierRetrieveDTO.builder()
                .courierName("Lalamove")
                .courierCode("lalamove")
                .courierServiceName("Instant")
                .courierServiceCode("instant")
                .serviceType("same_day")
                .shippingType("parcel")
                .description("Instant Delivery")
                .shipmentDurationRange("1 - 3")
                .shipmentDurationUnit("hours")
                .availableForInstantWaybillId(true)
                .build());
        list.add(CourierRetrieveDTO.builder()
                .courierName("JNE")
                .courierCode("jne")
                .courierServiceName("Reguler")
                .courierServiceCode("reg")
                .serviceType("regular")
                .shippingType("parcel")
                .description("Regular Service")
                .shipmentDurationRange("2 - 4")
                .shipmentDurationUnit("days")
                .availableForInstantWaybillId(false)
                .build());
        return CourierResponseDTO.builder()
                .success(true)
                .object("courier")
                .couriers(list)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_COURIERS, key = "'all'", unless = "#result == null")
    public CourierResponseDTO getAvailableCouriers() {
        log.debug("Starting getAvailableCouriers");
        try {
            if (stubEnabled()) {
                log.debug("SHIPPING_STUB enabled: returning stubbed couriers");
                return buildStubCouriers();
            }
            CourierResponseDTO response = biteshipShippingClient.getAvailableCouriers();
            if (response != null && response.getCouriers() != null) {
                // Filter to only allow gojek, lalamove, and jne as requested
                var filtered = response.getCouriers().stream()
                        .filter(c -> {
                            String code = c.getCourierCode();
                            if (code == null) return false;
                            String lc = code.trim().toLowerCase(Locale.ROOT);
                            return lc.equals("gojek") || lc.equals("lalamove") || lc.equals("jne");
                        })
                        .toList();
                response.setCouriers(filtered);
                log.debug("Filtered couriers to {} entries (gojek, lalamove, jne)", filtered.size());
            }
            log.debug("Successfully retrieved available couriers");
            return response;
        } catch (Exception e) {
            if (stubEnabled()) {
                log.warn("Falling back to stubbed couriers due to error: {}", e.getMessage());
                return buildStubCouriers();
            }
            log.error("Failed to get available couriers. Error: {}", e.getMessage(), e);
            throw new ShippingException("Failed to get available couriers: " + e.getMessage(), e);
        }
    }

    private CourierRateResponseDTO buildStubRates(CourierRateRequestDTO request) {
        // Very simple pricing model for development:
        // base per courier/service + (weight in kg rounded up * per-kg rate)
        int totalWeightGrams = 0;
        if (request != null && request.getItems() != null) {
            for (var it : request.getItems()) {
                int w = it.getWeight() != null ? it.getWeight() : 0;
                int q = it.getQuantity() != null ? it.getQuantity() : 1;
                totalWeightGrams += Math.max(0, w) * Math.max(1, q);
            }
        }
        int kg = Math.max(1, (int)Math.ceil(totalWeightGrams / 1000.0));
        String couriersStr = request != null ? request.getCouriers() : null;
        List<String> selected = new ArrayList<>();
        if (couriersStr != null && !couriersStr.isBlank()) {
            Arrays.stream(couriersStr.split(","))
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .filter(s -> !s.isBlank())
                    .forEach(selected::add);
        }
        if (selected.isEmpty()) {
            selected = Arrays.asList("gojek", "lalamove", "jne");
        }
        List<PricingDTO> pricing = new ArrayList<>();
        boolean isInstant = request != null && request.getType() != null && request.getType().equalsIgnoreCase("instant");

        for (String code : selected) {
            switch (code) {
                case "gojek" -> {
                    // Instant and Same Day
                    int baseInstant = 8000;
                    int perKgInstant = 6000;
                    int priceInstant = baseInstant + perKgInstant * kg;
                    pricing.add(PricingDTO.builder()
                            .courierName("Gojek")
                            .courierCode("gojek")
                            .courierServiceName(isInstant ? "Instant" : "Same Day")
                            .courierServiceCode(isInstant ? "instant" : "same_day")
                            .description(isInstant ? "On Demand Instant (bike)" : "On Demand within 8 hours (bike)")
                            .serviceType("same_day")
                            .shippingType("parcel")
                            .price(priceInstant)
                            .duration(isInstant ? "1 - 3 hours" : "6 - 8 hours")
                            .build());
                }
                case "lalamove" -> {
                    int baseInstant = 9000;
                    int perKgInstant = 5000;
                    int priceInstant = baseInstant + perKgInstant * kg;
                    pricing.add(PricingDTO.builder()
                            .courierName("Lalamove")
                            .courierCode("lalamove")
                            .courierServiceName("Instant")
                            .courierServiceCode("instant")
                            .description("Instant Delivery")
                            .serviceType("same_day")
                            .shippingType("parcel")
                            .price(priceInstant)
                            .duration("1 - 3 hours")
                            .build());
                }
                case "jne" -> {
                    int baseReg = 10000;
                    int perKgReg = 7000;
                    int priceReg = baseReg + perKgReg * kg;
                    pricing.add(PricingDTO.builder()
                            .courierName("JNE")
                            .courierCode("jne")
                            .courierServiceName("Reguler")
                            .courierServiceCode("reg")
                            .description("Regular Service")
                            .serviceType("regular")
                            .shippingType("parcel")
                            .price(priceReg)
                            .shipmentDurationRange("2 - 4")
                            .shipmentDurationUnit("days")
                            .build());
                }
                default -> {
                    // ignore unsupported in stub
                }
            }
        }
        return CourierRateResponseDTO.builder()
                .pricing(pricing)
                .build();
    }

    @Override
    public CourierRateResponseDTO getCourierRates(CourierRateRequestDTO request) {
        log.debug("Starting getCourierRates with request: {}", request);
        try {
            if (request == null) {
                log.debug("CourierRateRequestDTO is null");
                throw new ShippingException("CourierRateRequestDTO must not be null");
            }
            if (stubEnabled()) {
                log.debug("SHIPPING_STUB enabled: returning stubbed courier rates");
                return buildStubRates(request);
            }
            CourierRateResponseDTO response = biteshipShippingClient.getCourierRates(request);
            log.debug("Successfully retrieved courier rates");
            return response;
        } catch (Exception e) {
            if (stubEnabled()) {
                log.warn("Falling back to stubbed rates due to error: {}", e.getMessage());
                return buildStubRates(request);
            }
            log.error("Failed to get courier rates. Error: {}", e.getMessage(), e);
            throw new ShippingException("Failed to get courier rates: " + e.getMessage(), e);
        }
    }

    @Override
    public CreateOrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO) {
        log.debug("Starting createOrder with requestDTO: {}", requestDTO);
        try {
            if (requestDTO == null) {
                log.debug("CreateOrderRequestDTO is null");
                throw new ShippingException("CreateOrderRequestDTO must not be null");
            }
            CreateOrderResponseDTO response = biteshipShippingClient.createOrder(requestDTO);
            log.debug("Successfully created order");
            return response;
        } catch (Exception e) {
            log.error("Failed to create order. Error: {}", e.getMessage(), e);
            throw new ShippingException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Override
    public RetrieveOrderResponseDTO retrieveOrderById(String orderId) {
        log.debug("Starting retrieveOrderById with orderId: {}", orderId);
        try {
            if (orderId == null || orderId.isBlank()) {
                log.debug("Invalid orderId: {}", orderId);
                throw new ShippingException("Order ID must not be null or blank");
            }
            RetrieveOrderResponseDTO response = biteshipShippingClient.retrieveOrderById(orderId);
            log.debug("Successfully retrieved order for orderId: {}", orderId);
            return response;
        } catch (Exception e) {
            log.error("Failed to retrieve order for orderId: {}. Error: {}", orderId, e.getMessage(), e);
            throw new ShippingException("Failed to retrieve order: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_CANCELLATION_REASONS, key = "#lang != null ? #lang.trim().toLowerCase() : 'null'", unless = "#result == null")
    public CancellationReasonResponseDTO getCancellationReasons(String lang) {
        log.debug("Starting getCancellationReasons with lang: {}", lang);
        try {
            if (lang == null || lang.isBlank()) {
                log.debug("Invalid language code: {}", lang);
                throw new ShippingException("Language code must not be null or blank");
            }
            CancellationReasonResponseDTO response = biteshipShippingClient.getCancellationReasons(lang);
            log.debug("Successfully retrieved cancellation reasons for lang: {}", lang);
            return response;
        } catch (Exception e) {
            log.error("Failed to get cancellation reasons for lang: {}. Error: {}", lang, e.getMessage(), e);
            throw new ShippingException("Failed to get cancellation reasons: " + e.getMessage(), e);
        }
    }

    @Override
    public CancelOrderResponseDTO cancelOrder(String orderId, CancelOrderRequestDTO request) {
        log.debug("Starting cancelOrder with orderId: {}, request: {}", orderId, request);
        try {
            if (orderId == null || orderId.isBlank()) {
                log.debug("Invalid orderId: {}", orderId);
                throw new ShippingException("Order ID must not be null or blank");
            }
            if (request == null) {
                log.debug("CancelOrderRequestDTO is null");
                throw new ShippingException("CancelOrderRequestDTO must not be null");
            }
            CancelOrderResponseDTO response = biteshipShippingClient.cancelOrder(orderId, request);
            log.debug("Successfully cancelled order with orderId: {}", orderId);
            return response;
        } catch (Exception e) {
            log.error("Failed to cancel order for orderId: {}. Error: {}", orderId, e.getMessage(), e);
            throw new ShippingException("Failed to cancel order: " + e.getMessage(), e);
        }
    }

    @Override
    public TrackingResponseDTO getTrackingById(String trackingId) {
        log.debug("Starting getTrackingById with trackingId: {}", trackingId);
        try {
            if (trackingId == null || trackingId.isBlank()) {
                log.debug("Invalid trackingId: {}", trackingId);
                throw new ShippingException("Tracking ID must not be null or blank");
            }
            TrackingResponseDTO response = biteshipShippingClient.getTrackingById(trackingId);
            log.debug("Successfully retrieved tracking info for trackingId: {}", trackingId);
            return response;
        } catch (Exception e) {
            log.error("Failed to retrieve tracking info for trackingId: {}. Error: {}", trackingId, e.getMessage(), e);
            throw new ShippingException("Failed to retrieve tracking info: " + e.getMessage(), e);
        }
    }

    @Override
    public TrackingResponseDTO getPublicTracking(String waybillId, String courierCode) {
        log.debug("Starting getPublicTracking with waybillId: {}, courierCode: {}", waybillId, courierCode);
        try {
            if (waybillId == null || waybillId.isBlank()) {
                log.debug("Invalid waybillId: {}", waybillId);
                throw new ShippingException("Waybill ID must not be null or blank");
            }
            if (courierCode == null || courierCode.isBlank()) {
                log.debug("Invalid courierCode: {}", courierCode);
                throw new ShippingException("Courier code must not be null or blank");
            }
            TrackingResponseDTO response = biteshipShippingClient.getPublicTracking(waybillId, courierCode);
            log.debug("Successfully retrieved public tracking for waybillId: {}, courierCode: {}", waybillId, courierCode);
            return response;
        } catch (Exception e) {
            log.error("Failed to retrieve public tracking for waybillId: {}, courierCode: {}. Error: {}", waybillId, courierCode, e.getMessage(), e);
            throw new ShippingException("Failed to retrieve public tracking: " + e.getMessage(), e);
        }
    }
}
