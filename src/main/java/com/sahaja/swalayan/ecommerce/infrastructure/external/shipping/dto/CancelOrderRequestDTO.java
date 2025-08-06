package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequestDTO {
    @JsonProperty("cancellation_reason_code")
    private String cancellationReasonCode;

    @JsonProperty("cancellation_reason")
    private String cancellationReason;
}
