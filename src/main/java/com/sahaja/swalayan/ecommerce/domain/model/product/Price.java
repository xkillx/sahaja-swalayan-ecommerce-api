package com.sahaja.swalayan.ecommerce.domain.model.product;

import lombok.Value;
import java.math.BigDecimal;

@Value
public class Price {
    BigDecimal value;

    public Price(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        this.value = value;
    }
}

