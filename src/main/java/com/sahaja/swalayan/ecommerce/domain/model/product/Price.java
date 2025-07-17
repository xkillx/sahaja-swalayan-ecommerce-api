package com.sahaja.swalayan.ecommerce.domain.model.product;

import lombok.Value;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Value
public class Price {
    @NotNull(message = "Price value must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    BigDecimal value;

    public Price(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        this.value = value;
    }
}

