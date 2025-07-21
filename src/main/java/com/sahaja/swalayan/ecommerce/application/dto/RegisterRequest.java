package com.sahaja.swalayan.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User registration request")
public class RegisterRequest {
    @Schema(
        description = "User's full name",
        example = "John Doe",
        minLength = 2,
        maxLength = 100
    )
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Schema(
        description = "User's email address",
        example = "john.doe@example.com",
        format = "email"
    )
    @NotBlank
    @Email
    private String email;

    @Schema(
        description = "User's password",
        example = "SecurePassword123",
        minLength = 8,
        maxLength = 100
    )
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Schema(
        description = "User's phone number (optional)",
        example = "+6281234567890",
        pattern = "^\\+?[0-9]{10,15}$"
    )
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;
}
