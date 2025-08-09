package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Represents the origin section in the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OriginDTO {
    /**
     * The contact person's name at the origin location.
     */
    @JsonProperty("contact_name")
    private String contactName;

    /**
     * The contact phone number at the origin location.
     */
    @JsonProperty("contact_phone")
    private String contactPhone;

    /**
     * The geographic coordinate of the origin location.
     */
    private CoordinateDTO coordinate;

    /**
     * The address of the origin location.
     */
    private String address;

    /**
     * Additional note or instruction for the origin location.
     */
    private String note;

    /**
     * The postal code of the origin location.
     */
    @JsonProperty("postal_code")
    private Integer postalCode;
}
