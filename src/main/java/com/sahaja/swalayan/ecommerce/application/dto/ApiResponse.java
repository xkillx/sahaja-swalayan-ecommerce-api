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
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {
    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message describing the result", example = "Operation completed successfully")
    private String message;
    
    @Schema(description = "Response data payload (can be null)")
    private T data;
    
    @Schema(description = "Timestamp when the response was generated", example = "2025-01-21T12:56:03")
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
