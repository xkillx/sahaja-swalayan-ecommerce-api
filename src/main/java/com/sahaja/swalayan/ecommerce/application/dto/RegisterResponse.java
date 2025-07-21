package com.sahaja.swalayan.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration response")
public class RegisterResponse {
    @Schema(description = "Email address of the registered user", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Registration status message", example = "Registration successful. Please check your email for confirmation.")
    private String message;
    
    @Schema(description = "Indicates if email confirmation is required", example = "true")
    private boolean requiresConfirmation;

    public static RegisterResponse success(String email) {
        return RegisterResponse.builder()
                .email(email)
                .message("Registration successful. Please check your email for confirmation.")
                .requiresConfirmation(true)
                .build();
    }
}
