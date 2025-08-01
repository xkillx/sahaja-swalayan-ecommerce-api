package com.sahaja.swalayan.ecommerce.common;

public class InvalidXenditPayloadException extends RuntimeException {
    public InvalidXenditPayloadException(String message) {
        super(message);
    }
    
    public InvalidXenditPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
