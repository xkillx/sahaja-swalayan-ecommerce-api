package com.sahaja.swalayan.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull
    @JsonProperty("address_id")
    private UUID addressId;

    // Optional shipping selection fields from frontend
    @JsonProperty("shipping_courier_code")
    private String shippingCourierCode;

    @JsonProperty("shipping_courier_service")
    private String shippingCourierService;

    @JsonProperty("shipping_courier_service_name")
    private String shippingCourierServiceName;

    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost;
}
