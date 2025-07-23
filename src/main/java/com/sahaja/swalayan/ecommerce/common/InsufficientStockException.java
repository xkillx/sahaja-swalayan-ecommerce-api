package com.sahaja.swalayan.ecommerce.common;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
