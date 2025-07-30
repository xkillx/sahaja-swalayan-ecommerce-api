package com.sahaja.swalayan.ecommerce.common;

/**
 * Exception thrown when attempting to create an order from an empty cart.
 * Results in HTTP 400 Bad Request by default global handler.
 */
public class CartEmptyException extends RuntimeException {
    public CartEmptyException(String message) {
        super(message);
    }
}
