package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private int quantity;
    // Optionally add price, imageUrl, etc. as needed

    public static CartItemResponse fromEntity(CartItem cartItem) {
        if (cartItem == null) return null;
        if (cartItem.getProduct() == null) throw new IllegalStateException("CartItem has no product");
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
