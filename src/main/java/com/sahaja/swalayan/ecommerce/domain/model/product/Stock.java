package com.sahaja.swalayan.ecommerce.domain.model.product;

import lombok.Value;
import jakarta.validation.constraints.Min;

@Value
public class Stock {
    @Min(value = 0, message = "Stock must be non-negative")
    int value;

    public Stock(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Stock must be non-negative");
        }
        this.value = value;
    }
}
