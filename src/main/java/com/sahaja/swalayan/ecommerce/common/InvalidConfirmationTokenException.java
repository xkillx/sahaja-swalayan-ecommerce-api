package com.sahaja.swalayan.ecommerce.common;

public class InvalidConfirmationTokenException extends RuntimeException {
    public InvalidConfirmationTokenException(String message) {
        super(message);
    }
}
