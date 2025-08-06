package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the shipper section in the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipperDTO {
    /**
     * The name of the shipper.
     */
    private String name;

    /**
     * The email address of the shipper.
     */
    private String email;

    /**
     * The phone number of the shipper.
     */
    private String phone;

    /**
     * The organization or company name of the shipper.
     */
    private String organization;
}
