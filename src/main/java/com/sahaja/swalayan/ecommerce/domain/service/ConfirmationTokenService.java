package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.token.ConfirmationToken;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;

import java.util.Optional;
import java.util.UUID;

public interface ConfirmationTokenService {
    String createTokenForUser(User user);
    
    Optional<ConfirmationToken> findByToken(String token);
    
    boolean isTokenValid(String token);
    
    Optional<ConfirmationToken> findValidTokenByUserId(UUID userId);
    
    void confirmToken(String token);
    
    void deleteTokensForUser(UUID userId);
    
    void cleanupExpiredTokens();
}
