package com.sahaja.swalayan.ecommerce.domain.repository;

import com.sahaja.swalayan.ecommerce.domain.model.token.ConfirmationToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ConfirmationTokenRepository {
    
    ConfirmationToken save(ConfirmationToken token);
    
    Optional<ConfirmationToken> findById(UUID id);
    
    Optional<ConfirmationToken> findByToken(String token);
    
    Optional<ConfirmationToken> findByUserId(UUID userId);
    
    Optional<ConfirmationToken> findValidTokenByUserId(UUID userId, LocalDateTime now);
    
    void deleteExpiredTokens(LocalDateTime now);
    
    void deleteByUserId(UUID userId);
    
    void delete(ConfirmationToken token);
}
