package com.sahaja.swalayan.ecommerce.infrastructure.repository.user;

import com.sahaja.swalayan.ecommerce.domain.model.user.Address;
import com.sahaja.swalayan.ecommerce.domain.repository.user.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;

    @Autowired
    public AddressRepositoryImpl(AddressJpaRepository addressJpaRepository) {
        this.addressJpaRepository = addressJpaRepository;
    }

    @Override
    public List<Address> findAllByUserId(UUID userId) {
        return addressJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Address save(Address address) {
        return addressJpaRepository.save(address);
    }

    @Override
    public void deleteById(UUID id) {
        addressJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Address> findById(UUID id) {
        return addressJpaRepository.findById(id);
    }

    @Override
    public void delete(Address address) {
        addressJpaRepository.delete(address);
    }
}
