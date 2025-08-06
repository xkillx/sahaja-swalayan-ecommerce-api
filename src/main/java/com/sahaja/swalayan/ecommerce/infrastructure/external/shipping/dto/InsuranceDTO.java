package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing the insurance section under the courier object
 * in the Biteship Create Order response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceDTO {
    /**
     * The insured amount.
     */
    @JsonProperty("amount")
    private Integer amount;

    /**
     * The currency of the insured amount.
     */
    @JsonProperty("amount_currency")
    private String amountCurrency;

    /**
     * The insurance fee.
     */
    @JsonProperty("fee")
    private Integer fee;

    /**
     * The currency of the insurance fee.
     */
    @JsonProperty("fee_currency")
    private String feeCurrency;

    /**
     * Additional notes regarding the insurance.
     */
    @JsonProperty("note")
    private String note;
}
