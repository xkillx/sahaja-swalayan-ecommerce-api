# Email Provider System

The email service has been refactored to support multiple email providers through a pluggable architecture. This allows you to easily switch between different email providers without changing your business logic.

## Architecture

### Core Components

1. **EmailProvider Interface** - Abstraction for all email providers
2. **EmailRequest** - Provider-agnostic email request object
3. **EmailSendException** - Standardized exception for email failures
4. **EmailTemplateService** - Handles email content generation
5. **EmailServiceImpl** - Main service that uses the configured provider

### Provider Implementations

#### 1. Mailtrap Provider (Default)
- **Class**: `MailtrapEmailProvider`
- **Configuration**: `email.provider=mailtrap`
- **Dependencies**: `io.mailtrap:mailtrap-java:1.0.0`

#### 2. SMTP Provider
- **Class**: `SmtpEmailProvider`
- **Configuration**: `email.provider=smtp`
- **Dependencies**: `spring-boot-starter-mail` (not included by default)

## Configuration

### Current Configuration (application.yaml)
```yaml
# Email Configuration
email:
  provider: mailtrap  # Options: mailtrap, smtp, sendgrid, ses

# Mailtrap Configuration
mailtrap:
  api:
    token: your-api-token
  from:
    email: noreply@sahajaswalayan.com
    name: Sahaja Swalayan
```

### Environment Variables
```bash
# Email Provider Selection
EMAIL_PROVIDER=mailtrap

# Mailtrap Configuration
MAILTRAP_API_TOKEN=your-mailtrap-api-token
MAILTRAP_FROM_EMAIL=noreply@sahajaswalayan.com
MAILTRAP_FROM_NAME=Sahaja Swalayan
```

## Switching Email Providers

### To Mailtrap (Current)
1. Set `EMAIL_PROVIDER=mailtrap`
2. Configure Mailtrap API token
3. No additional dependencies needed

### To SMTP
1. Add dependency to `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-mail</artifactId>
   </dependency>
   ```
2. Set `EMAIL_PROVIDER=smtp`
3. Configure SMTP settings in `application.yaml`:
   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: your-email@gmail.com
       password: your-app-password
       properties:
         mail:
           smtp:
             auth: true
             starttls:
               enable: true
   ```

## Adding New Email Providers

To add a new email provider (e.g., SendGrid, AWS SES):

1. **Create Provider Implementation**:
   ```java
   @Component
   @ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid")
   public class SendGridEmailProvider implements EmailProvider {
       // Implementation
   }
   ```

2. **Add Dependencies** (if needed):
   ```xml
   <dependency>
       <groupId>com.sendgrid</groupId>
       <artifactId>sendgrid-java</artifactId>
   </dependency>
   ```

3. **Add Configuration**:
   ```yaml
   sendgrid:
     api:
       key: your-sendgrid-api-key
   ```

4. **Update Environment Variables**:
   ```bash
   EMAIL_PROVIDER=sendgrid
   SENDGRID_API_KEY=your-api-key
   ```

## Benefits

1. **Provider Independence**: Business logic doesn't depend on specific email providers
2. **Easy Switching**: Change providers with just configuration
3. **Testability**: Easy to mock EmailProvider for unit tests
4. **Extensibility**: Add new providers without modifying existing code
5. **Consistency**: Standardized error handling and logging across providers

## Testing

The system supports easy testing by mocking the `EmailProvider` interface:

```java
@MockBean
private EmailProvider emailProvider;

@Test
void testEmailSending() {
    // Mock the provider
    doNothing().when(emailProvider).sendEmail(any(EmailRequest.class));
    
    // Test your service
    emailService.sendConfirmationEmail("test@example.com", "token123");
    
    // Verify
    verify(emailProvider).sendEmail(any(EmailRequest.class));
}
```

## Current Status

- ✅ Mailtrap Provider (Active)
- ✅ SMTP Provider (Available, requires additional dependency)
- 🔄 SendGrid Provider (Can be added)
- 🔄 AWS SES Provider (Can be added)
- 🔄 Other providers (Can be added as needed)
