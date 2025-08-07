package com.sahaja.swalayan.ecommerce.application.mapper;

import com.sahaja.swalayan.ecommerce.application.dto.user.AddressDTO;
import com.sahaja.swalayan.ecommerce.application.dto.user.CreateAddressRequestDTO;
import com.sahaja.swalayan.ecommerce.domain.model.user.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    AddressDTO toDto(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toEntity(CreateAddressRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CreateAddressRequestDTO dto, @MappingTarget Address address);
}
