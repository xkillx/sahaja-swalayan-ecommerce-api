package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.service.EmailService;
import com.sahaja.swalayan.ecommerce.domain.service.EmailTemplateService;
import com.sahaja.swalayan.ecommerce.infrastructure.email.EmailProvider;
import com.sahaja.swalayan.ecommerce.infrastructure.email.EmailRequest;
import com.sahaja.swalayan.ecommerce.infrastructure.email.EmailSendException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final EmailProvider emailProvider;
    private final EmailTemplateService emailTemplateService;
    private final String fromEmail;
    private final String fromName;
    
    public EmailServiceImpl(
            EmailProvider emailProvider,
            EmailTemplateService emailTemplateService,
            @Value("${email.from.address}") String fromEmail,
            @Value("${email.from.name:Sahaja Swalayan}") String fromName) {
        
        this.emailProvider = emailProvider;
        this.emailTemplateService = emailTemplateService;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        
        log.info("EmailService initialized with provider: {}", emailProvider.getProviderName());
    }
    
    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendConfirmationEmailAsync(String to, String token) {
        try {
            sendConfirmationEmailInternal(to, token);
            log.info("Async confirmation email sent successfully to: {} via {}", to, emailProvider.getProviderName());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send async confirmation email to: {}. Error: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Override
    public void sendConfirmationEmail(String to, String token) {
        sendConfirmationEmailInternal(to, token);
    }
    
    private void sendConfirmationEmailInternal(String to, String token) {
        try {
            EmailRequest emailRequest = EmailRequest.builder()
                    .fromEmail(fromEmail)
                    .fromName(fromName)
                    .toEmail(to)
                    .subject("Confirm Your Email Address - Sahaja Swalayan")
                    .textContent(emailTemplateService.generateConfirmationTextContent(token))
                    .htmlContent(emailTemplateService.generateConfirmationHtmlContent(token))
                    .build();
            
            emailProvider.sendEmail(emailRequest);
            
            log.info("Confirmation email sent successfully to: {} via {}", to, emailProvider.getProviderName());
            
        } catch (EmailSendException e) {
            log.error("Failed to send confirmation email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }
}
