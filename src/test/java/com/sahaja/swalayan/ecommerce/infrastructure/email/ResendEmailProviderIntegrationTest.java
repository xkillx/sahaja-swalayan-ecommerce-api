package com.sahaja.swalayan.ecommerce.infrastructure.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ResendEmailProvider}.
 *
 * These tests are enabled only when RESEND_API_KEY is present in the environment.
 * They send a real email using Resend sample addresses. Make sure your account/domain
 * is configured properly before running.
 */
@SpringBootTest
class ResendEmailProviderIntegrationTest {

    @Autowired
    private EmailProvider emailProvider;

    @Test
    void sendEmail_success_sendsHtmlEmail() throws Exception {
        // given
        EmailRequest request = EmailRequest.builder()
                .fromEmail("onboarding@resend.dev")
                .fromName("Acme")
                .toEmail("azhar.tkjzone@gmail.com")
                .subject("Resend Integration Test - HTML")
                .htmlContent("<strong>hello world</strong>")
                .build();

        // then
        assertThat(emailProvider.getProviderName()).isEqualTo("Resend");
        assertThatCode(() -> emailProvider.sendEmail(request))
                .doesNotThrowAnyException();
    }

    @Test
    void sendEmail_invalidRequest_throwsEmailSendException() {
        // missing subject and content should fail validation
        EmailRequest badRequest = EmailRequest.builder()
                .fromEmail("onboarding@resend.dev")
                .toEmail("delivered@resend.dev")
                .subject("Missing content")
                .build();

        assertThat(emailProvider.getProviderName()).isEqualTo("Resend");
        assertThatThrownBy(() -> emailProvider.sendEmail(badRequest))
                .isInstanceOf(EmailSendException.class);
    }
}
