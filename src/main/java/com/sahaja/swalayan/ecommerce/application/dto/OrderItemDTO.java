package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemDTO {
    private UUID id;
    private UUID productId;
    private int quantity;
    private BigDecimal pricePerUnit;
}
