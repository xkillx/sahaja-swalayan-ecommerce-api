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
import com.sahaja.swalayan.ecommerce.common.ShippingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShippingServiceImpl implements ShippingService {

    private final BiteshipShippingClient biteshipShippingClient;

    @Autowired
    public ShippingServiceImpl(BiteshipShippingClient biteshipShippingClient) {
        this.biteshipShippingClient = biteshipShippingClient;
    }

    @Override
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

    @Override
    public CourierResponseDTO getAvailableCouriers() {
        log.debug("Starting getAvailableCouriers");
        try {
            CourierResponseDTO response = biteshipShippingClient.getAvailableCouriers();
            log.debug("Successfully retrieved available couriers");
            return response;
        } catch (Exception e) {
            log.error("Failed to get available couriers. Error: {}", e.getMessage(), e);
            throw new ShippingException("Failed to get available couriers: " + e.getMessage(), e);
        }
    }

    @Override
    public CourierRateResponseDTO getCourierRates(CourierRateRequestDTO request) {
        log.debug("Starting getCourierRates with request: {}", request);
        try {
            if (request == null) {
                log.debug("CourierRateRequestDTO is null");
                throw new ShippingException("CourierRateRequestDTO must not be null");
            }
            CourierRateResponseDTO response = biteshipShippingClient.getCourierRates(request);
            log.debug("Successfully retrieved courier rates");
            return response;
        } catch (Exception e) {
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
}
