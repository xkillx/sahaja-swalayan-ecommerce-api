package com.sahaja.swalayan.ecommerce.domain.service;

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

public interface ShippingService {
    /**
     * Search areas (e.g., for address autocomplete or region lookup).
     * @param input search query
     * @return AreaResponseDTO with area details
     */
    AreaResponseDTO searchAreas(String input);

    /**
     * Get available couriers.
     * @return CourierResponseDTO with courier list
     */
    CourierResponseDTO getAvailableCouriers();

    /**
     * Get courier rates for a shipping request.
     * @param request CourierRateRequestDTO
     * @return CourierRateResponseDTO with rate options
     */
    CourierRateResponseDTO getCourierRates(CourierRateRequestDTO request);

    /**
     * Create a shipping order.
     * @param requestDTO CreateOrderRequestDTO
     * @return CreateOrderResponseDTO with order details
     */
    CreateOrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO);

    /**
     * Retrieve a shipping order by its ID.
     * @param orderId order ID
     * @return RetrieveOrderResponseDTO with order info
     */
    RetrieveOrderResponseDTO retrieveOrderById(String orderId);

    /**
     * Get cancellation reasons for orders.
     * @param lang language code (e.g., "en", "id")
     * @return CancellationReasonResponseDTO
     */
    CancellationReasonResponseDTO getCancellationReasons(String lang);

    /**
     * Cancel a shipping order.
     * @param orderId order ID
     * @param request CancelOrderRequestDTO
     * @return CancelOrderResponseDTO
     */
    CancelOrderResponseDTO cancelOrder(String orderId, CancelOrderRequestDTO request);

    /**
     * Retrieve tracking details by tracking ID.
     * @param trackingId tracking ID
     * @return TrackingResponseDTO with tracking info
     */
    TrackingResponseDTO getTrackingById(String trackingId);
}

