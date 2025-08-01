package com.sahaja.swalayan.ecommerce.common.exception;

public class InvalidXenditWebhookException extends RuntimeException {

    public InvalidXenditWebhookException(String message) {
        super(message);
    }

    public InvalidXenditWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
