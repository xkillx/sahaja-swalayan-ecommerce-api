package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing the delivery section
 * in the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {
    /**
     * The delivery date and time in ISO8601 format.
     */
    @JsonProperty("datetime")
    private String datetime;

    /**
     * Additional notes regarding the delivery.
     */
    @JsonProperty("note")
    private String note;

    /**
     * The type of delivery, either "now" or "scheduled".
     */
    @JsonProperty("type")
    private String type;

    /**
     * The distance for the delivery (e.g., in kilometers or miles).
     */
    @JsonProperty("distance")
    private Double distance;

    /**
     * The unit of distance (e.g., "km", "mi").
     */
    @JsonProperty("distance_unit")
    private String distanceUnit;
}
