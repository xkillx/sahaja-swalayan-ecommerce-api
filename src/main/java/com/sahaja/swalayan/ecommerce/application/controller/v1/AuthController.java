package com.sahaja.swalayan.ecommerce.application.controller.v1;

import java.util.HashMap;
import java.util.List;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.ConfirmResponse;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
import com.sahaja.swalayan.ecommerce.application.dto.RegisterResponse;
import com.sahaja.swalayan.ecommerce.application.dto.LoginRequest;
import com.sahaja.swalayan.ecommerce.application.dto.LoginResponse;
import com.sahaja.swalayan.ecommerce.application.dto.GoogleLoginRequest;
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
    
    private final com.sahaja.swalayan.ecommerce.application.service.GoogleTokenVerifier googleTokenVerifier;
    private final com.sahaja.swalayan.ecommerce.application.service.FirebaseTokenVerifier firebaseTokenVerifier;
    
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final com.sahaja.swalayan.ecommerce.domain.repository.UserRepository userRepository;
    private final com.sahaja.swalayan.ecommerce.infrastructure.repository.UserJpaRepository userJpaRepository;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil,
            com.sahaja.swalayan.ecommerce.application.service.GoogleTokenVerifier googleTokenVerifier,
            com.sahaja.swalayan.ecommerce.application.service.FirebaseTokenVerifier firebaseTokenVerifier,
            com.sahaja.swalayan.ecommerce.domain.repository.UserRepository userRepository,
            com.sahaja.swalayan.ecommerce.infrastructure.repository.UserJpaRepository userJpaRepository
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.googleTokenVerifier = googleTokenVerifier;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
        this.userRepository = userRepository;
        this.userJpaRepository = userJpaRepository;
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
            LoginResponse resp = new LoginResponse(token);
            resp.setUserId(customUserDetails.getId().toString());
            resp.setEmail(customUserDetails.getEmail());
            resp.setName(customUserDetails.getUser().getName());
            resp.setRole(customUserDetails.getRole());
            return ResponseEntity.ok(resp);
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new LoginResponse(null, "Bad credentials"));
        } catch (org.springframework.security.authentication.DisabledException ex) {
            return ResponseEntity.status(403).body(new LoginResponse(null, "User account is not active. Please confirm your email before logging in."));
        }
    }
    
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        var result = googleTokenVerifier.verify(request.getIdToken());
        if (!result.valid()) {
            return ResponseEntity.status(401).body(new LoginResponse(null, result.error() != null ? result.error() : "Invalid Google token"));
        }
        // Find or create user by email
        var userOpt = userRepository.findByEmail(result.email());
        com.sahaja.swalayan.ecommerce.domain.model.user.User user;
        if (userOpt.isEmpty()) {
            // Create a new user with ACTIVE status and random password
            String randomPwd = java.util.UUID.randomUUID().toString();
            String hashed = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(randomPwd);
            user = com.sahaja.swalayan.ecommerce.domain.model.user.User.builder()
                    .name(result.name())
                    .email(result.email())
                    .passwordHash(hashed)
                    .role(com.sahaja.swalayan.ecommerce.domain.model.user.UserRole.CUSTOMER)
                    .status(com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.ACTIVE)
                    .build();
            user = userRepository.save(user);
        } else {
            user = userOpt.get();
            // If user was pending, activate upon Google verified login
            if (user.getStatus() == com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.PENDING) {
                user.setStatus(com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.ACTIVE);
                user = userRepository.save(user);
            }
        }
        var claims = new java.util.HashMap<String, Object>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("roles", java.util.List.of(user.getRole()));
        String token = jwtTokenUtil.generateToken(user.getEmail(), claims);
        LoginResponse resp = new LoginResponse(token);
        resp.setUserId(user.getId().toString());
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setRole(user.getRole().name());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/firebase/exchange")
    public ResponseEntity<LoginResponse> firebaseExchange(@Valid @RequestBody GoogleLoginRequest request) {
        var result = firebaseTokenVerifier.verify(request.getIdToken());
        if (!result.valid()) {
            return ResponseEntity.status(401).body(new LoginResponse(null, result.error() != null ? result.error() : "Invalid Firebase token"));
        }
        var userOpt = userRepository.findByEmail(result.email());
        com.sahaja.swalayan.ecommerce.domain.model.user.User user;
        if (userOpt.isEmpty()) {
            String randomPwd = java.util.UUID.randomUUID().toString();
            String hashed = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(randomPwd);
            user = com.sahaja.swalayan.ecommerce.domain.model.user.User.builder()
                    .name(result.name())
                    .email(result.email())
                    .passwordHash(hashed)
                    .role(com.sahaja.swalayan.ecommerce.domain.model.user.UserRole.CUSTOMER)
                    .status(com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.ACTIVE)
                    .build();
            user = userRepository.save(user);
        } else {
            user = userOpt.get();
            if (user.getStatus() == com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.PENDING) {
                user.setStatus(com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.ACTIVE);
                user = userRepository.save(user);
            }
        }
        var claims = new java.util.HashMap<String, Object>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("roles", java.util.List.of(user.getRole()));
        claims.put("provider", result.provider());
        String token = jwtTokenUtil.generateToken(user.getEmail(), claims);
        LoginResponse resp = new LoginResponse(token);
        resp.setUserId(user.getId().toString());
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setRole(user.getRole().name());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> adminRegister(@Valid @RequestBody RegisterRequest request) {
        long adminCount = userJpaRepository.countByRole(com.sahaja.swalayan.ecommerce.domain.model.user.UserRole.ADMIN);
        if (adminCount > 0) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud) ||
                    cud.getUser().getRole() != com.sahaja.swalayan.ecommerce.domain.model.user.UserRole.ADMIN) {
                return ResponseEntity.status(403).body(ApiResponse.error("Forbidden"));
            }
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already registered"));
        }
        String hashed = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(request.getPassword());
        var user = com.sahaja.swalayan.ecommerce.domain.model.user.User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(hashed)
                .role(com.sahaja.swalayan.ecommerce.domain.model.user.UserRole.ADMIN)
                .status(com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", RegisterResponse.success(request.getEmail())));
    }
    
    private com.sahaja.swalayan.ecommerce.domain.repository.UserRepository authServiceUserRepo() {
        // small helper to access the UserRepository indirectly through CustomUserDetailsService dependency or via context
        // Since AuthController already has AuthenticationManager and JwtTokenUtil, we inject UserRepository using application context
        // but to keep this minimal without refactoring constructors widely, we resolve it via Spring's ApplicationContextProvider if present.
        // However, simpler: leverage CustomUserDetailsService's repo is not accessible here. So autowire directly.
        throw new IllegalStateException("UserRepository bean accessor not replaced during code generation");
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        var user = cud.getUser();
        var map = new java.util.HashMap<String, Object>();
        map.put("userId", user.getId().toString());
        map.put("email", user.getEmail());
        map.put("name", user.getName());
        map.put("role", user.getRole());
        map.put("status", user.getStatus());
        return ResponseEntity.ok(ApiResponse.success("OK", map));
    }
}
