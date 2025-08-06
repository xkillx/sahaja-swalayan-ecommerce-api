package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing Cash On Delivery (COD) details
 * in the Biteship Create Order API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashOnDeliveryDTO {
    /**
     * Unique identifier for the COD transaction.
     */
    @JsonProperty("id")
    private String id;

    /**
     * The COD amount to be collected.
     */
    @JsonProperty("amount")
    private Integer amount;

    /**
     * The currency of the COD amount.
     */
    @JsonProperty("amount_currency")
    private String amountCurrency;

    /**
     * The fee charged for COD service.
     */
    @JsonProperty("fee")
    private Integer fee;

    /**
     * The currency of the COD fee.
     */
    @JsonProperty("fee_currency")
    private String feeCurrency;

    /**
     * Additional notes regarding the COD transaction.
     */
    @JsonProperty("note")
    private String note;

    /**
     * Type of COD settlement period (e.g., "3_days", "5_days", "7_days").
     */
    @JsonProperty("type")
    private String type;
}
