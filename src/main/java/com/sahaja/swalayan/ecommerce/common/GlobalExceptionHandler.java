package com.sahaja.swalayan.ecommerce.common;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.jsonwebtoken.JwtException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Exception: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error("An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.error("Entity Not Found: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ProductNotFoundException.class, CartItemNotFoundException.class, CartNotFoundException.class})
    public ResponseEntity<ApiResponse<?>> handleCartRelatedNotFound(RuntimeException ex, WebRequest request) {
        log.error("Cart Related Not Found: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientStock(InsufficientStockException ex, WebRequest request) {
        log.error("Insufficient Stock: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailAlreadyRegisteredException(EmailAlreadyRegisteredException ex, WebRequest request) {
        log.error("Email Already Registered: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidConfirmationTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidConfirmationTokenException(InvalidConfirmationTokenException ex, WebRequest request) {
        log.error("Invalid Confirmation Token: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException ex, WebRequest request) {
        log.error("Invalid JWT token: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error("Invalid JWT token");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleMalformedJson(org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request) {
        log.error("Malformed JSON: {}", ex.getMessage());
        ApiResponse<?> error = ApiResponse.error("Malformed JSON request");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
