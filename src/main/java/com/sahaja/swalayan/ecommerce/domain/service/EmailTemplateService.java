package com.sahaja.swalayan.ecommerce.domain.service;

/**
 * Service interface for generating email templates
 * This allows for different template implementations (e.g., static templates, template engines, etc.)
 */
public interface EmailTemplateService {
    
    /**
     * Generate text content for confirmation email
     * @param token the confirmation token
     * @return plain text email content
     */
    String generateConfirmationTextContent(String token);
    
    /**
     * Generate HTML content for confirmation email
     * @param token the confirmation token
     * @return HTML email content
     */
    String generateConfirmationHtmlContent(String token);
}
