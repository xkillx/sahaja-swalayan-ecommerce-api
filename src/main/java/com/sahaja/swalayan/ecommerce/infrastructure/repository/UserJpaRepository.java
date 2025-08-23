package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByRole(com.sahaja.swalayan.ecommerce.domain.model.user.UserRole role);

    @Query("select u from User u where lower(u.name) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%')) or u.phone like concat('%', :q, '%')")
    Page<User> search(@Param("q") String q, Pageable pageable);
}
