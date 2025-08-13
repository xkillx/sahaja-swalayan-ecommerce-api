package com.sahaja.swalayan.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
    @NotNull(message = "address_id must not be null")
    @JsonProperty("address_id")
    private UUID addressId;

    // Optional shipping selection fields from frontend
    @NotBlank(message = "shipping_courier_code must not be blank")
    @JsonProperty("shipping_courier_code")
    private String shippingCourierCode;

    @NotBlank(message = "shipping_courier_service must not be blank")
    @JsonProperty("shipping_courier_service")
    private String shippingCourierService;

    @NotBlank(message = "shipping_courier_service_name must not be blank")
    @JsonProperty("shipping_courier_service_name")
    private String shippingCourierServiceName;

    @NotNull(message = "shipping_cost must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "shipping_cost must be greater than 0")
    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost;
}
