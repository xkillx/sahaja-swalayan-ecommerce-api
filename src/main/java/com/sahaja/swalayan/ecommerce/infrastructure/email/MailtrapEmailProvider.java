package com.sahaja.swalayan.ecommerce.infrastructure.email;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * Mailtrap implementation of EmailProvider
 * This implementation will only be active when email.provider=mailtrap
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "mailtrap", matchIfMissing = true)
public class MailtrapEmailProvider implements EmailProvider {
    
    private final MailtrapClient mailtrapClient;
    private final boolean sandboxEnabled;
    private final String sandboxInboxId;
    
    public MailtrapEmailProvider(
            @Value("${mailtrap.api.token}") String apiToken,
            @Value("${mailtrap.sandbox.enabled:true}") boolean sandboxEnabled,
            @Value("${mailtrap.sandbox.inbox-id:}") String sandboxInboxId) {
        
        this.sandboxEnabled = sandboxEnabled;
        this.sandboxInboxId = sandboxInboxId;
        
        MailtrapConfig.Builder configBuilder = new MailtrapConfig.Builder()
                .token(apiToken);
        
        // Configure for sandbox or production mode
        if (sandboxEnabled) {
            // Sandbox mode - emails captured in Mailtrap dashboard
            configBuilder.sandbox(true);
            configBuilder.inboxId(Long.valueOf(sandboxInboxId));
            // Note: Inbox ID is configured through Mailtrap dashboard or API token settings
        } else {
            // Production mode - emails will be delivered to real recipients
            configBuilder.sandbox(false);
        }
        
        MailtrapConfig config = configBuilder.build();
        
        this.mailtrapClient = MailtrapClientFactory.createMailtrapClient(config);
        
        if (sandboxEnabled) {
            log.info("MailtrapEmailProvider initialized in SANDBOX mode with inbox ID: {}", 
                    StringUtils.hasText(sandboxInboxId) ? sandboxInboxId : "default");
        } else {
            log.info("MailtrapEmailProvider initialized in PRODUCTION mode");
        }
    }
    
    @Override
    public void sendEmail(EmailRequest emailRequest) throws EmailSendException {
        try {
            emailRequest.validate();
            
            // Log sandbox mode information
            if (sandboxEnabled) {
                log.debug("Sending email in SANDBOX mode - emails will not be delivered to real recipients");
                if (StringUtils.hasText(sandboxInboxId)) {
                    log.debug("Using sandbox inbox ID: {}", sandboxInboxId);
                }
            }
            
            // Create MailtrapMail using the builder pattern
            var mailBuilder = MailtrapMail.builder()
                    .from(new Address(emailRequest.getFromEmail(), emailRequest.getFromName()))
                    .to(List.of(new Address(emailRequest.getToEmail(), emailRequest.getToName())))
                    .subject(emailRequest.getSubject());
            
            // Note: Mailtrap SDK automatically handles sandbox mode based on API token
            // Inbox ID is managed through Mailtrap dashboard configuration
            
            if (emailRequest.getTextContent() != null && !emailRequest.getTextContent().trim().isEmpty()) {
                mailBuilder.text(emailRequest.getTextContent());
            }
            
            if (emailRequest.getHtmlContent() != null && !emailRequest.getHtmlContent().trim().isEmpty()) {
                mailBuilder.html(emailRequest.getHtmlContent());
            }
            
            MailtrapMail mail = mailBuilder.build();
            mailtrapClient.send(mail);
            
            if (sandboxEnabled) {
                log.info("Email sent successfully via Mailtrap SANDBOX to: {} (not delivered to real recipient)", 
                        emailRequest.getToEmail());
            } else {
                log.info("Email sent successfully via Mailtrap PRODUCTION to: {}", emailRequest.getToEmail());
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid email request: {}", e.getMessage());
            throw new EmailSendException("Invalid email request: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send email via Mailtrap to: {}. Error: {}", 
                     emailRequest.getToEmail(), e.getMessage(), e);
            throw new EmailSendException("Failed to send email via Mailtrap", e);
        }
    }
    
    @Override
    public String getProviderName() {
        return "Mailtrap";
    }
}
