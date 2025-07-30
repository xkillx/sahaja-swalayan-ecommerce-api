package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrderRequest {
    @NotBlank
    private String shippingAddress;
    @NotBlank
    private String paymentMethod;
}
