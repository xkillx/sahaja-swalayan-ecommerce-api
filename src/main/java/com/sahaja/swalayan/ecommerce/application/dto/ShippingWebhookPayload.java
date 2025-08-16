package com.sahaja.swalayan.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal payload for handling shipping provider (e.g., Biteship) webhooks.
 * We only need identifiers and status to update the corresponding order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingWebhookPayload {
    /** Shipment/order id from courier provider (e.g., Biteship order id) */
    @JsonProperty("id")
    private String id;

    /** Our reference id if we set when creating order (we set orderId as reference) */
    @JsonProperty("reference_id")
    private String referenceId;

    /** Tracking number / waybill */
    @JsonProperty("tracking_id")
    private String trackingId;

    /** Shipment status string from provider */
    @JsonProperty("status")
    private String status;
}
