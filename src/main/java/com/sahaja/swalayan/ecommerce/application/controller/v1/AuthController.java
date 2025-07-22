package com.sahaja.swalayan.ecommerce.application.controller.v1;

import java.util.HashMap;
import java.util.List;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.ConfirmResponse;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterResponse;
import com.sahaja.swalayan.ecommerce.application.dto.LoginRequest;
import com.sahaja.swalayan.ecommerce.application.dto.LoginResponse;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetails;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.sahaja.swalayan.ecommerce.domain.service.AuthService;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiAuthResponses;
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
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    @ApiAuthResponses
    @ApiSuccessResponseWithExample(
        description = "User logged in successfully",
        exampleName = "Login Success",
        example = """
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
          "tokenType": "Bearer"
        }
        """
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Extract CustomUserDetails
            var principal = authentication.getPrincipal();
            if (!(principal instanceof CustomUserDetails customUserDetails)) {
                throw new IllegalStateException("Unexpected principal type");
            }

            // Check user status
            if (!customUserDetails.isEnabled()) {
                return ResponseEntity.status(403).body(
                    new LoginResponse(null, "User account is not active. Please confirm your email before logging in.")
                );
            }

            var claims = new HashMap<String, Object>();
            claims.put("userId", customUserDetails.getId().toString());
            claims.put("email", customUserDetails.getEmail());
            claims.put("roles", List.of(customUserDetails.getRole()));

            String token = jwtTokenUtil.generateToken(customUserDetails.getUsername(), claims);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new LoginResponse(null, "Bad credentials"));
        } catch (org.springframework.security.authentication.DisabledException ex) {
            return ResponseEntity.status(403).body(new LoginResponse(null, "User account is not active. Please confirm your email before logging in."));
        }
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
