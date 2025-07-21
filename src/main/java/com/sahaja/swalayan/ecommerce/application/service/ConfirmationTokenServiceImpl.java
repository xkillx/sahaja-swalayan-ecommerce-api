package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.token.ConfirmationToken;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.repository.ConfirmationTokenRepository;
import com.sahaja.swalayan.ecommerce.domain.service.ConfirmationTokenService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final SecureRandom secureRandom;
    
    // Token expiration time in hours
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    
    public ConfirmationTokenServiceImpl(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String createTokenForUser(User user) {
        // Delete any existing tokens for this user
        confirmationTokenRepository.deleteByUserId(user.getId());
        
        // Generate a secure random token
        String token = generateSecureToken();
        
        // Create confirmation token entity
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(token)
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS))
                .build();
        
        // Save token to database
        confirmationTokenRepository.save(confirmationToken);
        
        return token;
    }
    
    @Transactional(readOnly = true)
    public Optional<ConfirmationToken> findByToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }
    
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByToken(token);
        return confirmationToken.isPresent() && !confirmationToken.get().isExpired();
    }
    
    @Transactional(readOnly = true)
    public Optional<ConfirmationToken> findValidTokenByUserId(UUID userId) {
        return confirmationTokenRepository.findValidTokenByUserId(userId, LocalDateTime.now());
    }
    
    public void confirmToken(String token) {
        Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByToken(token);
        
        if (confirmationToken.isEmpty()) {
            throw new IllegalArgumentException("Token not found");
        }
        
        if (confirmationToken.get().isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }
        
        // Delete the token after successful confirmation
        confirmationTokenRepository.delete(confirmationToken.get());
    }
    
    public void deleteTokensForUser(UUID userId) {
        confirmationTokenRepository.deleteByUserId(userId);
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        confirmationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
