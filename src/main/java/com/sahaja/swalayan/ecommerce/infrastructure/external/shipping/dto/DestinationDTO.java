package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing the destination section
 * in the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationDTO {
    /**
     * Name of the contact person at the destination.
     */
    @JsonProperty("contact_name")
    private String contactName;

    /**
     * Phone number of the contact person at the destination.
     */
    @JsonProperty("contact_phone")
    private String contactPhone;

    /**
     * Email address of the contact person at the destination.
     */
    @JsonProperty("contact_email")
    private String contactEmail;

    /**
     * Full address of the destination.
     */
    @JsonProperty("address")
    private String address;

    /**
     * Additional notes for the destination.
     */
    @JsonProperty("note")
    private String note;

    /**
     * Proof of delivery details for the destination.
     */
    @JsonProperty("proof_of_delivery")
    private ProofOfDeliveryDTO proofOfDelivery;

    /**
     * Cash on delivery details for the destination.
     */
    @JsonProperty("cash_on_delivery")
    private CashOnDeliveryDTO cashOnDelivery;

    /**
     * Geographic coordinates of the destination.
     */
    @JsonProperty("coordinate")
    private CoordinateDTO coordinate;

    /**
     * Postal code of the destination area.
     */
    @JsonProperty("postal_code")
    private Integer postalCode;
}
