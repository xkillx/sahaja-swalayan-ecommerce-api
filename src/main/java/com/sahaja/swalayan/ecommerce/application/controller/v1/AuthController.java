package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.ConfirmResponse;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterResponse;
import com.sahaja.swalayan.ecommerce.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and sends a confirmation email. The user must confirm their email before they can log in.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(
                    name = "Registration Example",
                    value = """
                    {
                        "name": "John Doe",
                        "email": "john.doe@example.com",
                        "password": "SecurePassword123",
                        "phone": "+6281234567890"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "Registration completed successfully",
                        "data": {
                            "email": "john.doe@example.com",
                            "message": "Registration successful. Please check your email for confirmation.",
                            "requiresConfirmation": true
                        },
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                        "success": false,
                        "message": "Validation failed",
                        "data": null,
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email already registered",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Email Conflict",
                    value = """
                    {
                        "success": false,
                        "message": "Email already registered: john.doe@example.com",
                        "data": null,
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                    {
                        "success": false,
                        "message": "An unexpected error occurred",
                        "data": null,
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        RegisterResponse data = RegisterResponse.success(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Registration completed successfully", data));
    }
    
    @GetMapping("/confirm")
    @Operation(
        summary = "Confirm user email",
        description = "Confirms a user's email address using the confirmation token sent via email during registration.",
        parameters = @Parameter(
            name = "token",
            description = "Email confirmation token received via email",
            required = true,
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Email confirmed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "Email confirmation completed successfully",
                        "data": {
                            "confirmed": true,
                            "message": "Email confirmed successfully",
                            "confirmedAt": "2025-01-21T12:56:03"
                        },
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired confirmation token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Invalid Token",
                    value = """
                    {
                        "success": false,
                        "message": "Invalid or expired confirmation token",
                        "data": null,
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                    {
                        "success": false,
                        "message": "An unexpected error occurred",
                        "data": null,
                        "timestamp": "2025-01-21T12:56:03"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ConfirmResponse>> confirm(@RequestParam("token") String token) {
        authService.confirmUser(token);
        ConfirmResponse data = ConfirmResponse.success();
        return ResponseEntity.ok(ApiResponse.success("Email confirmation completed successfully", data));
    }
}
