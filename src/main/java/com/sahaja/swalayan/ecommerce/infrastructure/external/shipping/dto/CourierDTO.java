package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Object representing courier details
 * from the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourierDTO {
    /**
     * Tracking ID for the shipment.
     */
    @JsonProperty("tracking_id")
    private String trackingId;

    /**
     * Waybill ID for the shipment.
     */
    @JsonProperty("waybill_id")
    private String waybillId;

    /**
     * Courier company name.
     */
    @JsonProperty("company")
    private String company;

    /**
     * Name of the assigned driver.
     */
    @JsonProperty("driver_name")
    private String driverName;

    /**
     * Phone number of the driver.
     */
    @JsonProperty("driver_phone")
    private String driverPhone;

    /**
     * URL to the driver's photo.
     */
    @JsonProperty("driver_photo_url")
    private String driverPhotoUrl;

    /**
     * Vehicle plate number of the driver.
     */
    @JsonProperty("driver_plate_number")
    private String driverPlateNumber;

    /**
     * Type of courier service.
     */
    @JsonProperty("type")
    private String type;

    /**
     * Link to the courier tracking or details page.
     */
    @JsonProperty("link")
    private String link;

    /**
     * Insurance details associated with the courier.
     */
    @JsonProperty("insurance")
    private InsuranceDTO insurance;

    /**
     * Routing code for the courier.
     */
    @JsonProperty("routing_code")
    private String routingCode;
}
