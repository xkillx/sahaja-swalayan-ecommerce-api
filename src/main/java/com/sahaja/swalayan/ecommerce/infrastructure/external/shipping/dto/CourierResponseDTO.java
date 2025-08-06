package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import java.util.List;

/**
 * DTO representing the response from Biteship Retrieve a Courier API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourierResponseDTO {
    private boolean success;
    private String object;

    @Singular
    @JsonProperty("couriers")
    private List<CourierRetrieveDTO> couriers;
}
