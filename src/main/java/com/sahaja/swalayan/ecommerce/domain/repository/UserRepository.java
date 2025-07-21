package com.sahaja.swalayan.ecommerce.domain.repository;

import com.sahaja.swalayan.ecommerce.domain.model.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    void deleteById(UUID id);
    void delete(User user);
}
