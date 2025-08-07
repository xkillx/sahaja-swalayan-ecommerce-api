package com.sahaja.swalayan.ecommerce.infrastructure.repository.user;

import com.sahaja.swalayan.ecommerce.domain.model.user.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressJpaRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
