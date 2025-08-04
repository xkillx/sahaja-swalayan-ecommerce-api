package com.sahaja.swalayan.ecommerce.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when there is not enough quantity for a product to fulfill a request.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientQuantityException extends RuntimeException {
    public InsufficientQuantityException(String message) {
        super(message);
    }
}
