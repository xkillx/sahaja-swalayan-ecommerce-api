package com.sahaja.swalayan.ecommerce.infrastructure.email;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Email request object that contains all information needed to send an email
 * This is provider-agnostic and can be used with any email provider implementation
 */
@Getter
@Builder
public class EmailRequest {
    
    @NonNull
    private final String fromEmail;
    
    private final String fromName;
    
    @NonNull
    private final String toEmail;
    
    private final String toName;
    
    @NonNull
    private final String subject;
    
    private final String textContent;
    
    private final String htmlContent;
    
    /**
     * Validates that the email request has the minimum required fields
     */
    public void validate() {
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("From email is required");
        }
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("To email is required");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if ((textContent == null || textContent.trim().isEmpty()) && 
            (htmlContent == null || htmlContent.trim().isEmpty())) {
            throw new IllegalArgumentException("Either text content or HTML content is required");
        }
    }
}
