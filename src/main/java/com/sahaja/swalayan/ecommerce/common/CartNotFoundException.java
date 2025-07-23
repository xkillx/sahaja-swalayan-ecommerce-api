package com.sahaja.swalayan.ecommerce.common;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }
}
