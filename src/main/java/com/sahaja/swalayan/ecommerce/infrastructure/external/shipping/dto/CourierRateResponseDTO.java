package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourierRateResponseDTO {
    private AddressDTO origin;
    private AddressDTO destination;
    @Singular("pricing")
    private List<PricingDTO> pricing;
}
