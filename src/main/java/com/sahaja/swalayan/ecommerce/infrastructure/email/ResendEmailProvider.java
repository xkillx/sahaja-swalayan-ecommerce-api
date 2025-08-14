package com.sahaja.swalayan.ecommerce.infrastructure.email;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Resend implementation of EmailProvider.
 * Activate by setting `email.provider=resend`.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "resend")
public class ResendEmailProvider implements EmailProvider {

    private final Resend resendClient;

    public ResendEmailProvider(@Value("${resend.api.key}") String apiKey) {
        this.resendClient = new Resend(apiKey);
        log.debug("ResendEmailProvider initialized");
    }

    @Override
    public void sendEmail(EmailRequest emailRequest) throws EmailSendException {
        try {
            emailRequest.validate();

            String from = buildFrom(emailRequest);
            String to = buildTo(emailRequest);

            CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(emailRequest.getSubject());

            if (emailRequest.getHtmlContent() != null && !emailRequest.getHtmlContent().isBlank()) {
                builder.html(emailRequest.getHtmlContent());
            }
            if (emailRequest.getTextContent() != null && !emailRequest.getTextContent().isBlank()) {
                builder.text(emailRequest.getTextContent());
            }

            CreateEmailOptions params = builder.build();

            CreateEmailResponse response = resendClient.emails().send(params);
            log.debug("Email sent successfully via Resend to: {} (id={})", emailRequest.getToEmail(), response.getId());
        } catch (IllegalArgumentException e) {
            log.error("Invalid email request: {}", e.getMessage());
            throw new EmailSendException("Invalid email request: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send email via Resend to: {}. Error: {}", emailRequest.getToEmail(), e.getMessage(), e);
            throw new EmailSendException("Failed to send email via Resend", e);
        }
    }

    @Override
    public String getProviderName() {
        return "Resend";
    }

    private String buildFrom(EmailRequest emailRequest) {
        if (emailRequest.getFromName() != null && !emailRequest.getFromName().isBlank()) {
            return emailRequest.getFromName() + " <" + emailRequest.getFromEmail() + ">";
        }
        return emailRequest.getFromEmail();
    }

    private String buildTo(EmailRequest emailRequest) {
        if (emailRequest.getToName() != null && !emailRequest.getToName().isBlank()) {
            return emailRequest.getToName() + " <" + emailRequest.getToEmail() + ">";
        }
        return emailRequest.getToEmail();
    }
}
