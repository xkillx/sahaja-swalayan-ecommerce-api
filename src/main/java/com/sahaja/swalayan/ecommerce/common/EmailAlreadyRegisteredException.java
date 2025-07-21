package com.sahaja.swalayan.ecommerce.common;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
    
    public EmailAlreadyRegisteredException(String email, String message) {
        super(String.format("Email '%s' %s", email, message));
    }
}
