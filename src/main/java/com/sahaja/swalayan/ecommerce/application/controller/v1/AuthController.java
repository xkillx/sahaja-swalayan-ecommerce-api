package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.ConfirmResponse;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterResponse;
import com.sahaja.swalayan.ecommerce.domain.service.AuthService;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiConfirmationOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiRegistrationOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiSuccessResponseWithExample;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @ApiRegistrationOperation
    @ApiSuccessResponseWithExample(
        description = "User registered successfully",
        exampleName = "Registration Success",
        example = """
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
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        RegisterResponse data = RegisterResponse.success(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Registration completed successfully", data));
    }
    
    @GetMapping("/confirm")
    @ApiConfirmationOperation
    @ApiSuccessResponseWithExample(
        description = "Email confirmed successfully",
        exampleName = "Confirmation Success",
        example = """
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
    public ResponseEntity<ApiResponse<ConfirmResponse>> confirm(@RequestParam("token") String token) {
        authService.confirmUser(token);
        ConfirmResponse data = ConfirmResponse.success();
        return ResponseEntity.ok(ApiResponse.success("Email confirmation completed successfully", data));
    }
}
