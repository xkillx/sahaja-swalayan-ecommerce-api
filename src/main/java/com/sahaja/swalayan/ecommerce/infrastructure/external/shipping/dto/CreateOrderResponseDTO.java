package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing the response body of the Biteship "Create Order" API (POST /v1/orders).
 * <p>
 * Includes all top-level fields and references to nested DTOs for complex objects.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderResponseDTO {
    /** Indicates if the request was successful. */
    @JsonProperty("success")
    private boolean success;

    /** Response message from Biteship. */
    @JsonProperty("message")
    private String message;

    /** Object type, e.g., "order". */
    @JsonProperty("object")
    private String object;

    /** Unique order ID. */
    @JsonProperty("id")
    private String id;

    /** Draft order ID, if applicable. */
    @JsonProperty("draft_order_id")
    private String draftOrderId;

    /** Shipper details. */
    @JsonProperty("shipper")
    private ShipperDTO shipper;

    /** Origin details. */
    @JsonProperty("origin")
    private OriginDTO origin;

    /** Destination details. */
    @JsonProperty("destination")
    private DestinationDTO destination;

    /** Courier details. */
    @JsonProperty("courier")
    private CourierDTO courier;

    /** Delivery details. */
    @JsonProperty("delivery")
    private DeliveryDTO delivery;

    /** Reference ID for the order. */
    @JsonProperty("reference_id")
    private String referenceId;

    /** List of order items. */
    @JsonProperty("items")
    @Singular
    private List<OrderItemDTO> items;

    /** List of extra objects (structure may vary). */
    @JsonProperty("extra")
    @Singular("extra")
    private List<Object> extra;

    /** Currency code for the order (e.g., "IDR"). */
    @JsonProperty("currency")
    private String currency;

    /** List of tax line objects (structure may vary). */
    @JsonProperty("tax_lines")
    @Singular("taxLines")
    private List<Object> taxLines;

    /** Total price of the order. */
    @JsonProperty("price")
    private Integer price;

    /** Additional metadata for the order. */
    @JsonProperty("metadata")
    
    private Map<String, Object> metadata;

    /** Additional notes for the order. */
    @JsonProperty("note")
    private String note;

    /** Status of the order (e.g., "pending", "confirmed"). */
    @JsonProperty("status")
    private String status;
}
