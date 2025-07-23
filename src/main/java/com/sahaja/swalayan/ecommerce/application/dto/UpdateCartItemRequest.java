package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequest {
    @Min(1)
    private int quantity;
}
