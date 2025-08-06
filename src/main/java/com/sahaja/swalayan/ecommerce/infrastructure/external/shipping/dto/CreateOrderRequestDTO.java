package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a Create Order request payload for Biteship API (POST /v1/orders).
 * <p>
 * This DTO encapsulates all required and optional fields for order creation, including shipper, origin,
 * destination, courier, items, and additional options as per Biteship's specification.
 * <p>
 * Use this DTO for outbound requests to Biteship's order creation endpoint.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequestDTO {
    /**
     * Unique reference ID for this order (optional, for idempotency).
     */
    private String referenceId;

    /**
     * Metadata for the order (optional).
     */
    private Map<String, Object> metadata;

    /**
     * Tags for this order (optional).
     */
    @Singular
    private List<String> tags;

    /**
     * Shipper details (optional, may be required for some couriers).
     */
    private String shipperName;
    private String shipperPhone;
    private String shipperEmail;
    private String shipperOrganization;

    /**
     * Origin contact details (required).
     */
    private String originContactName;
    private String originContactPhone;
    private String originContactEmail;
    private String originContactAddress;
    private String originContactPostalCode;
    private String originContactAreaId;
    /**
     * Origin coordinate (required, latitude & longitude).
     */
    private CoordinateDTO originCoordinate;

    /**
     * Destination contact details (required).
     */
    private String destinationContactName;
    private String destinationContactPhone;
    private String destinationContactEmail;
    private String destinationContactAddress;
    private String destinationContactPostalCode;
    private String destinationContactAreaId;
    /**
     * Destination coordinate (required, latitude & longitude).
     */
    private CoordinateDTO destinationCoordinate;

    /**
     * Courier information (required).
     */
    private String courierCompany;
    private String courierType;
    /**
     * Courier insurance (optional).
     */
    private Boolean courierInsurance;

    /**
     * Delivery type: "now" or "scheduled" (required).
     */
    private String deliveryType;
    /**
     * Scheduled delivery time (optional, required if deliveryType is "scheduled").
     * Format: ISO 8601 date-time string (e.g., 2025-08-06T10:00:00+07:00)
     */
    private String deliveryTime;

    /**
     * Cash on Delivery (COD) options (optional).
     */
    private Integer codAmount;
    private String codType;

    /**
     * List of items in the order (required, at least one item).
     */
    @Singular
    private List<OrderItemDTO> items;
}
