package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.token.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmationTokenJpaRepository extends JpaRepository<ConfirmationToken, UUID> {
    
    Optional<ConfirmationToken> findByToken(String token);
    
    Optional<ConfirmationToken> findByUserId(UUID userId);
    
    @Query("SELECT ct FROM ConfirmationToken ct WHERE ct.userId = :userId AND ct.expiresAt > :now")
    Optional<ConfirmationToken> findValidTokenByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM ConfirmationToken ct WHERE ct.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    void deleteByUserId(UUID userId);
}
