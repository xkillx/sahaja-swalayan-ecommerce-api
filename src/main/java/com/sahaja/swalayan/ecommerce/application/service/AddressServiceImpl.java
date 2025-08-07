package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.application.dto.user.AddressDTO;
import com.sahaja.swalayan.ecommerce.application.dto.user.CreateAddressRequestDTO;
import com.sahaja.swalayan.ecommerce.application.mapper.AddressMapper;
import com.sahaja.swalayan.ecommerce.common.exception.AddressNotFoundException;
import com.sahaja.swalayan.ecommerce.domain.model.user.Address;
import com.sahaja.swalayan.ecommerce.domain.repository.user.AddressRepository;
import com.sahaja.swalayan.ecommerce.domain.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;


    @Override
    public List<AddressDTO> getUserAddresses(UUID userId) {
        log.debug("Fetching addresses for userId {}", userId);
        List<Address> addresses = addressRepository.findAllByUserId(userId);
        List<AddressDTO> dtos = addresses.stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Found {} addresses for userId {}", dtos.size(), userId);
        return dtos;
    }

    @Override
    public AddressDTO saveAddress(UUID userId, CreateAddressRequestDTO dto) {
        log.debug("Saving new address for userId {}", userId);
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            log.debug("Setting new default address. Unsetting previous default for userId {}", userId);
            unsetDefaultAddress(userId);
        }
        Address address = Address.builder()
                .userId(userId)
                .label(dto.getLabel())
                .contactName(dto.getContactName())
                .contactPhone(dto.getContactPhone())
                .addressLine(dto.getAddressLine())
                .postalCode(dto.getPostalCode())
                .areaId(dto.getAreaId())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .createdAt(LocalDateTime.now())
                .build();
        Address saved = addressRepository.save(address);
        log.debug("Address saved with id {} for userId {}", saved.getId(), userId);
        return addressMapper.toDto(saved);
    }

    @Override
    public AddressDTO updateAddress(UUID userId, UUID addressId, CreateAddressRequestDTO dto) {
        log.debug("Updating address {} for userId {}", addressId, userId);
        Address address = addressRepository.findById(addressId)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new AddressNotFoundException("Address not found or does not belong to user"));
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            log.debug("Setting address {} as default. Unsetting previous default for userId {}", addressId, userId);
            unsetDefaultAddress(userId);
        }
        address.setLabel(dto.getLabel());
        address.setContactName(dto.getContactName());
        address.setContactPhone(dto.getContactPhone());
        address.setAddressLine(dto.getAddressLine());
        address.setPostalCode(dto.getPostalCode());
        address.setAreaId(dto.getAreaId());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        address.setUpdatedAt(LocalDateTime.now());
        Address updated = addressRepository.save(address);
        log.debug("Address updated with id {} for userId {}", updated.getId(), userId);
        return addressMapper.toDto(updated);
    }

    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        log.debug("Deleting address {} for userId {}", addressId, userId);
        Address address = addressRepository.findById(addressId)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new AddressNotFoundException("Address not found or does not belong to user"));
        addressRepository.delete(address);
        log.debug("Address deleted with id {} for userId {}", addressId, userId);
    }

    private void unsetDefaultAddress(UUID userId) {
        List<Address> addresses = addressRepository.findAllByUserId(userId);
        for (Address addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addr.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(addr);
            }
        }
    }
}
