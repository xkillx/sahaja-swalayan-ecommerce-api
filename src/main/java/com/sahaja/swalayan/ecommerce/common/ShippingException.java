package com.sahaja.swalayan.ecommerce.common;

import lombok.Getter;

@Getter
public class ShippingException extends RuntimeException {
    private final String message;

    public ShippingException(String message) {
        super(message);
        this.message = message;
    }

    public ShippingException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
