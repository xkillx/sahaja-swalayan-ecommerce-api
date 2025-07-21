package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;
import com.sahaja.swalayan.ecommerce.common.EmailAlreadyRegisteredException;
import com.sahaja.swalayan.ecommerce.common.InvalidConfirmationTokenException;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.service.AuthService;
import com.sahaja.swalayan.ecommerce.domain.service.ConfirmationTokenService;
import com.sahaja.swalayan.ecommerce.domain.service.EmailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            ConfirmationTokenService confirmationTokenService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            throw new EmailAlreadyRegisteredException(request.getEmail(), "is already registered");
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.PENDING)
                .build();
        user = userRepository.save(user);
        String token = confirmationTokenService.createTokenForUser(user);
        
        // Send confirmation email asynchronously to improve response time
        emailService.sendConfirmationEmailAsync(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void confirmUser(String token) {
        if (!confirmationTokenService.isTokenValid(token)) {
            throw new InvalidConfirmationTokenException("Invalid or expired confirmation token");
        }
        
        // Get the token to find the associated user
        var confirmationToken = confirmationTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidConfirmationTokenException("Invalid or expired confirmation token"));
        
        // Update user status to ACTIVE
        User user = userRepository.findById(confirmationToken.getUserId())
                .orElseThrow(() -> new InvalidConfirmationTokenException("User not found for token"));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        // Delete the confirmation token
        confirmationTokenService.confirmToken(token);
    }
}
