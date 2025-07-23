package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private UUID id;
    private UUID userId;
    private List<CartItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartResponse fromEntity(Cart cart) {
        if (cart == null) return null;
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(cart.getItems().stream()
                        .map(CartItemResponse::fromEntity)
                        .toList())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
