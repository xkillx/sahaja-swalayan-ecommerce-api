package com.sahaja.swalayan.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Email confirmation response")
public class ConfirmResponse {
    @Schema(description = "Indicates if the email was successfully confirmed", example = "true")
    private boolean confirmed;
    
    @Schema(description = "Confirmation status message", example = "Email confirmed successfully. Your account is now active.")
    private String message;
    
    @Schema(description = "Timestamp when the email was confirmed", example = "2025-01-21T12:56:03")
    private LocalDateTime confirmedAt;

    public static ConfirmResponse success() {
        return ConfirmResponse.builder()
                .confirmed(true)
                .message("Email confirmed successfully. Your account is now active.")
                .confirmedAt(LocalDateTime.now())
                .build();
    }
}
