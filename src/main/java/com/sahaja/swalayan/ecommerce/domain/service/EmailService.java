package com.sahaja.swalayan.ecommerce.domain.service;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    CompletableFuture<Void> sendConfirmationEmailAsync(String to, String token);
    
    // Synchronous method for backward compatibility
    void sendConfirmationEmail(String to, String token);

    // Send a generic HTML email with dynamic to/subject/htmlContent
    void sendHtmlEmail(String to, String subject, String htmlContent);
}
