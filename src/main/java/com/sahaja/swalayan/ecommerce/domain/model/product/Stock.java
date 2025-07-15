package com.sahaja.swalayan.ecommerce.domain.model.product;

import lombok.Value;

@Value
public class Stock {
    int value;

    public Stock(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Stock must be non-negative");
        }
        this.value = value;
    }
}
