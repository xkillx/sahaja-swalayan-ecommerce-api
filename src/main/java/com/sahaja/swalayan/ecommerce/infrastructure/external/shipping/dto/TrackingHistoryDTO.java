package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingHistoryDTO {
    @JsonProperty("note")
    private String note;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("status")
    private String status;
}
