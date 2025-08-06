package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a geographical coordinate (latitude and longitude).
 * <p>
 * Used for origin and destination coordinates in Biteship Create Order API.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateDTO {
    /**
     * Latitude of the location.
     */
    private Double latitude;

    /**
     * Longitude of the location.
     */
    private Double longitude;
}
