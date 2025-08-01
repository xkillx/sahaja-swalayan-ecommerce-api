package com.sahaja.swalayan.ecommerce.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Exception thrown when a payment cannot be found by ID
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(UUID id) {
        super("Payment not found with ID: " + id);
    }
}
