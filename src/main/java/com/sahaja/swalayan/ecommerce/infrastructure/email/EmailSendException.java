package com.sahaja.swalayan.ecommerce.infrastructure.email;

/**
 * Exception thrown when email sending fails
 */
public class EmailSendException extends Exception {
    
    public EmailSendException(String message) {
        super(message);
    }
    
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EmailSendException(Throwable cause) {
        super(cause);
    }
}
