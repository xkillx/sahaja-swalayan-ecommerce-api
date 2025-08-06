package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancellationReasonResponseDTO {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @Singular("cancellationReason")
    @JsonProperty("cancellation_reasons")
    private List<CancellationReasonDTO> cancellationReasons;
}
