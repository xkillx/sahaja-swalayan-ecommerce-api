package com.sahaja.swalayan.ecommerce.domain.repository.user;

import com.sahaja.swalayan.ecommerce.domain.model.user.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository {
    List<Address> findAllByUserId(UUID userId);
    Address save(Address address);
    void deleteById(UUID id);
    void delete(Address address);
    Optional<Address> findById(UUID id);
}
