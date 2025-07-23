package com.sahaja.swalayan.ecommerce.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity;
}
