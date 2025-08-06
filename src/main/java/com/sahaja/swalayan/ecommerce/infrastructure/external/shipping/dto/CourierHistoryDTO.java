package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierHistoryDTO {
    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("status")
    private String status;

    @JsonProperty("note")
    private String note;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}
