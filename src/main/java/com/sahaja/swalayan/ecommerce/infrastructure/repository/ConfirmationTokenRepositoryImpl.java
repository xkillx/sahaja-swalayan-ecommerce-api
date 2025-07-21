package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.token.ConfirmationToken;
import com.sahaja.swalayan.ecommerce.domain.repository.ConfirmationTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ConfirmationTokenRepositoryImpl implements ConfirmationTokenRepository {
    
    private final ConfirmationTokenJpaRepository jpaRepository;

    public ConfirmationTokenRepositoryImpl(ConfirmationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ConfirmationToken save(ConfirmationToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<ConfirmationToken> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ConfirmationToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public Optional<ConfirmationToken> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<ConfirmationToken> findValidTokenByUserId(UUID userId, LocalDateTime now) {
        return jpaRepository.findValidTokenByUserId(userId, now);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens(LocalDateTime now) {
        jpaRepository.deleteExpiredTokens(now);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void delete(ConfirmationToken token) {
        jpaRepository.delete(token);
    }
}
