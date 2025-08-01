package com.sahaja.swalayan.ecommerce.infrastructure.xendit;

public class XenditInvoiceCreationException extends RuntimeException {
    public XenditInvoiceCreationException(String message) {
        super(message);
    }
    public XenditInvoiceCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
