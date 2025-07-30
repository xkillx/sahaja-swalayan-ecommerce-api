package com.sahaja.swalayan.ecommerce.common;

/**
 * Exception thrown when an invalid payment method is provided.
 */
public class InvalidPaymentMethodException extends RuntimeException {
    public InvalidPaymentMethodException(String message) {
        super(message);
    }
}
