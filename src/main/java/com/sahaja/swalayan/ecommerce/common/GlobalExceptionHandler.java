package com.sahaja.swalayan.ecommerce.common;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Exception: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error("An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle all NOT_FOUND exceptions
    @ExceptionHandler({
        EntityNotFoundException.class,
        ProductNotFoundException.class,
        CartItemNotFoundException.class,
        CartNotFoundException.class,
        PaymentNotFoundException.class,
        OrderNotFoundException.class
    })
    public ResponseEntity<ApiResponse<?>> handleNotFound(RuntimeException ex, WebRequest request) {
        log.error("Not Found: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("Malformed JSON request: {}", ex.getMessage());
        String message = "Malformed JSON request: " + ex.getMostSpecificCause().getMessage();
        ApiResponse<?> error = ApiResponse.error(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle all BAD_REQUEST exceptions
    @ExceptionHandler({
        InsufficientStockException.class,
        InvalidConfirmationTokenException.class,
        InvalidPaymentMethodException.class,
        InvalidXenditPayloadException.class,
        JwtException.class
    })
    public ResponseEntity<ApiResponse<?>> handleBadRequest(Exception ex, WebRequest request) {
        log.error("Bad Request ({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle CONFLICT
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiResponse<?>> handleConflict(EmailAlreadyRegisteredException ex, WebRequest request) {
        log.error("Conflict: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Handle FORBIDDEN
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(ForbiddenException ex, WebRequest request) {
        log.error("Forbidden: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    // Handle UNAUTHORIZED for webhook token validation
    @ExceptionHandler(InvalidXenditWebhookException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedWebhook(InvalidXenditWebhookException ex, WebRequest request) {
        log.error("Unauthorized webhook request: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error("Webhook authentication failed: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex,
            WebRequest request) {
        StringBuilder errorMessages = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorMessages.append(fieldName).append(": ").append(errorMessage).append("; ");
        });
        String message = errorMessages.toString().trim();
        if (message.endsWith(";")) {
            message = message.substring(0, message.length() - 1);
        }
        ApiResponse<?> error = ApiResponse.error(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
