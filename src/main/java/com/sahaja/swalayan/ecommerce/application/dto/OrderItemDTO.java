package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemDTO {
    private UUID id;
    @JsonProperty("product_id")
    private UUID productId;
    private int quantity;
    @JsonProperty("price_per_unit")
    private BigDecimal pricePerUnit;
}
