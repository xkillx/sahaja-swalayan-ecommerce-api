package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Default implementation of EmailTemplateService
 * Uses static templates with string formatting for email content generation
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {
    
    private final String confirmationBaseUrl;
    
    public EmailTemplateServiceImpl(
            @Value("${app.confirmation.base-url:http://localhost:8080/api/v1/auth/confirm}") String confirmationBaseUrl) {
        this.confirmationBaseUrl = confirmationBaseUrl;
    }
    
    /**
     * Generate text content for confirmation email
     */
    @Override
    public String generateConfirmationTextContent(String token) {
        String confirmationUrl = confirmationBaseUrl + "?token=" + token;
        
        return String.format(
            "Welcome to Sahaja Swalayan!\n\n" +
            "Thank you for registering with us. To complete your registration, " +
            "please click on the link below to confirm your email address:\n\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create this account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The Sahaja Swalayan Team",
            confirmationUrl
        );
    }
    
    /**
     * Generate HTML content for confirmation email
     */
    @Override
    public String generateConfirmationHtmlContent(String token) {
        String confirmationUrl = confirmationBaseUrl + "?token=" + token;
        
        return String.format(
            "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>Email Confirmation</title>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
            "        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }" +
            "        .button { display: inline-block; background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
            "        .button:hover { background-color: #45a049; }" +
            "        .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
            "        .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 10px; border-radius: 4px; margin: 15px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class='header'>" +
            "        <h1>Welcome to Sahaja Swalayan!</h1>" +
            "    </div>" +
            "    <div class='content'>" +
            "        <h2>Confirm Your Email Address</h2>" +
            "        <p>Thank you for registering with Sahaja Swalayan. To complete your registration and start shopping with us, please confirm your email address by clicking the button below:</p>" +
            "        <div style='text-align: center;'>" +
            "            <a href='%s' class='button'>Confirm Email Address</a>" +
            "        </div>" +
            "        <p>Or copy and paste this link into your browser:</p>" +
            "        <p style='word-break: break-all; background-color: #f0f0f0; padding: 10px; border-radius: 4px;'>%s</p>" +
            "        <div class='warning'>" +
            "            <strong>Important:</strong> This confirmation link will expire in 24 hours. If you did not create this account, please ignore this email." +
            "        </div>" +
            "        <p>If you have any questions, feel free to contact our support team.</p>" +
            "        <p>Best regards,<br>The Sahaja Swalayan Team</p>" +
            "    </div>" +
            "    <div class='footer'>" +
            "        <p>&copy; 2024 Sahaja Swalayan. All rights reserved.</p>" +
            "    </div>" +
            "</body>" +
            "</html>",
            confirmationUrl, confirmationUrl
        );
    }
}
