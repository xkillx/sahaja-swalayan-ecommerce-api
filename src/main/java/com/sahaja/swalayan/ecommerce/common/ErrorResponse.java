package com.sahaja.swalayan.ecommerce.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Error response structure")
public class ErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2025-01-21T12:56:03")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "HTTP status text", example = "Bad Request")
    private String error;
    
    @Schema(description = "Error message describing what went wrong", example = "Invalid input data")
    private String message;
    
    @Schema(description = "Request path where the error occurred", example = "/v1/auth/register")
    private String path;

    public ErrorResponse() {}

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
