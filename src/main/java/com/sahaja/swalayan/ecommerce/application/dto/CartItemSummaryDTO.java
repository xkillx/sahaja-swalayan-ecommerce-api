package com.sahaja.swalayan.ecommerce.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemSummaryDTO(
        UUID id, UUID productId, String name,
        BigDecimal price, Integer quantity, BigDecimal subtotal, String imageUrl
) {}
