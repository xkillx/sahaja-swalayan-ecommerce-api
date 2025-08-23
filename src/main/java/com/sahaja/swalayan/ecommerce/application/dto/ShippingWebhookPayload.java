package com.sahaja.swalayan.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for handling shipping provider (e.g., Biteship) webhooks.
 * Includes identifiers, status, and optional courier/driver details for richer customer information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingWebhookPayload {
    /** Shipment/order id from courier provider (e.g., Biteship order id) */
    @JsonProperty("id")
    private String id;

    /** Some Biteship payloads include order_id instead of id */
    @JsonProperty("order_id")
    private String orderId;

    /** Our reference id if we set when creating order (we set orderId as reference) */
    @JsonProperty("reference_id")
    private String referenceId;

    /** Tracking number / waybill (some payloads use courier_tracking_id) */
    @JsonProperty("tracking_id")
    private String trackingId;

    @JsonProperty("courier_tracking_id")
    private String courierTrackingId;

    /** Shipment status string from provider */
    @JsonProperty("status")
    private String status;

    // --- Additional optional fields from provider (Biteship) to enrich customer info ---
    @JsonProperty("courier_waybill_id")
    private String courierWaybillId;

    @JsonProperty("courier_company")
    private String courierCompany;

    @JsonProperty("courier_type")
    private String courierType;

    @JsonProperty("courier_driver_name")
    private String courierDriverName;

    @JsonProperty("courier_driver_phone")
    private String courierDriverPhone;

    @JsonProperty("courier_driver_plate_number")
    private String courierDriverPlateNumber;

    @JsonProperty("courier_driver_photo_url")
    private String courierDriverPhotoUrl;

    @JsonProperty("courier_link")
    private String courierLink;

    /** ISO time when status updated (provider time) */
    @JsonProperty("updated_at")
    private String updatedAt;

    // Explicit getters for compatibility with any mapping issues
    public String getCourierWaybillId() { return courierWaybillId; }
    public String getCourierCompany() { return courierCompany; }
    public String getCourierType() { return courierType; }
    public String getCourierDriverName() { return courierDriverName; }
    public String getCourierDriverPhone() { return courierDriverPhone; }
    public String getCourierDriverPlateNumber() { return courierDriverPlateNumber; }
    public String getCourierDriverPhotoUrl() { return courierDriverPhotoUrl; }
    public String getCourierLink() { return courierLink; }
    public String getUpdatedAt() { return updatedAt; }
}
