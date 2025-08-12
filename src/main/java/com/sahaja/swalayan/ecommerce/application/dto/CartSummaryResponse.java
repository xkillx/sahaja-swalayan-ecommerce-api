package com.sahaja.swalayan.ecommerce.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartSummaryResponse(
        UUID cartId, List<CartItemSummaryDTO> items, BigDecimal total
) {}
