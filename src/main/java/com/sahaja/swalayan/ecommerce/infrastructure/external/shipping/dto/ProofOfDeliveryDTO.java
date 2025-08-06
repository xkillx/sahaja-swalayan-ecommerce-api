package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing the proof of delivery section
 * in the destination object of the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProofOfDeliveryDTO {
    /**
     * Indicates whether proof of delivery is used.
     */
    @JsonProperty("use")
    private Boolean use;

    /**
     * The fee for proof of delivery service.
     */
    @JsonProperty("fee")
    private Integer fee;

    /**
     * Additional notes regarding proof of delivery.
     */
    @JsonProperty("note")
    private String note;

    /**
     * Link to the proof of delivery document or resource.
     */
    @JsonProperty("link")
    private String link;
}
