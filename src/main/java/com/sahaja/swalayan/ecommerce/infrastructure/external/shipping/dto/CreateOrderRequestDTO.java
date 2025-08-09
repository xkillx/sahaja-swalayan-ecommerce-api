package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing a Create Order request payload for Biteship
 * API (POST /v1/orders).
 * <p>
 * This DTO encapsulates all required and optional fields for order creation,
 * including shipper, origin,
 * destination, courier, items, and additional options as per Biteship's
 * specification.
 * <p>
 * Use this DTO for outbound requests to Biteship's order creation endpoint.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderRequestDTO {
    /**
     * Unique reference ID for this order (optional, for idempotency).
     */
    @JsonProperty("reference_id")
    private String referenceId;

    /**
     * Metadata for the order (optional).
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Tags for this order (optional).
     */
    @JsonProperty("tags")
    @Singular
    private List<String> tags;

    /**
     * Shipper details (optional, may be required for some couriers).
     */
    @JsonProperty("shipper_contact_name")
    private String shipperContactName;
    @JsonProperty("shipper_contact_phone")
    private String shipperContactPhone;
    @JsonProperty("shipper_contact_email")
    private String shipperContactEmail;
    @JsonProperty("shipper_organization")
    private String shipperOrganization;

    /**
     * Origin contact details (required).
     */
    @JsonProperty("origin_contact_name")
    private String originContactName;
    @JsonProperty("origin_contact_phone")
    private String originContactPhone;
    @JsonProperty("origin_contact_email")
    private String originContactEmail;
    @JsonProperty("origin_address")
    private String originAddress;
    @JsonProperty("origin_note")
    private String originNote;
    @JsonProperty("origin_postal_code")
    private String originPostalCode;
    @JsonProperty("origin_area_id")
    private String originAreaId;
    @JsonProperty("origin_location_id")
    private String originLocationId;
    @JsonProperty("origin_collection_method")
    private String originCollectionMethod;
    /**
     * Origin coordinate (required, latitude & longitude).
     */
    @JsonProperty("origin_coordinate")
    private CoordinateDTO originCoordinate;

    /**
     * Destination contact details (required).
     */
    @JsonProperty("destination_contact_name")
    private String destinationContactName;
    @JsonProperty("destination_contact_phone")
    private String destinationContactPhone;
    @JsonProperty("destination_contact_email")
    private String destinationContactEmail;
    @JsonProperty("destination_address")
    private String destinationAddress;
    @JsonProperty("destination_postal_code")
    private String destinationPostalCode;
    @JsonProperty("destination_area_id")
    private String destinationAreaId;
    @JsonProperty("destination_location_id")
    private String destinationLocationId;
    @JsonProperty("destination_cash_on_delivery")
    private Integer destinationCashOnDelivery;
    @JsonProperty("destination_cash_on_delivery_type")
    private String destinationCashOnDeliveryType;
    @JsonProperty("destination_proof_of_delivery")
    private Boolean destinationProofOfDelivery;
    @JsonProperty("destination_proof_of_delivery_note")
    private String destinationProofOfDeliveryNote;
    /**
     * Destination coordinate (required, latitude & longitude).
     */
    @JsonProperty("destination_coordinate")
    private CoordinateDTO destinationCoordinate;

    /**
     * Courier information (required).
     */
    @JsonProperty("courier_company")
    private String courierCompany;
    @JsonProperty("courier_type")
    private String courierType;
    /**
     * Courier insurance amount (optional).
     */
    @JsonProperty("courier_insurance")
    private Integer courierInsurance;

    /**
     * Delivery type: "now" or "scheduled" (required).
     */
    @JsonProperty("delivery_type")
    private String deliveryType;
    /**
     * The delivery date format: "YYYY-MM-DD" (optional, for scheduled).
     */
    @JsonProperty("delivery_date")
    private String deliveryDate;
    /**
     * The delivery time format: "HH:mm" (optional, for scheduled).
     */
    @JsonProperty("delivery_time")
    private String deliveryTime;

    /**
     * Additional information for the shipment.
     */
    @JsonProperty("order_note")
    private String orderNote;

    /**
     * List of items in the order (required, at least one item).
     */
    @JsonProperty("items")
    @Singular
    private List<OrderItemDTO> items;
}
