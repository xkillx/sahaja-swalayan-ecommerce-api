package com.sahaja.swalayan.ecommerce.infrastructure.email;

/**
 * Interface for email providers to abstract away the specific implementation
 * This allows switching between different email providers (Mailtrap, SendGrid, AWS SES, etc.)
 */
public interface EmailProvider {
    
    /**
     * Send an email
     * @param emailRequest the email request containing all necessary information
     * @throws EmailSendException if email sending fails
     */
    void sendEmail(EmailRequest emailRequest) throws EmailSendException;
    
    /**
     * Get the provider name for logging purposes
     * @return provider name
     */
    String getProviderName();
}
