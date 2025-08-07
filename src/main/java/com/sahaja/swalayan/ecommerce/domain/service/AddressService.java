package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.application.dto.user.AddressDTO;
import com.sahaja.swalayan.ecommerce.application.dto.user.CreateAddressRequestDTO;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    List<AddressDTO> getUserAddresses(UUID userId);
    AddressDTO saveAddress(UUID userId, CreateAddressRequestDTO dto);
    AddressDTO updateAddress(UUID userId, UUID addressId, CreateAddressRequestDTO dto);
    void deleteAddress(UUID userId, UUID addressId);
}
