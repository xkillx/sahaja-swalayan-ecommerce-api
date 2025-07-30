package com.sahaja.swalayan.ecommerce.application.mapper;

import com.sahaja.swalayan.ecommerce.application.dto.OrderDTO;
import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;
import java.util.List;

import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(source = "items", target = "items")
    OrderDTO toOrderDTO(Order order);
    List<OrderDTO> toOrderDTOs(List<Order> orders);
}
