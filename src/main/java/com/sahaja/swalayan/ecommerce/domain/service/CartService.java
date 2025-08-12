package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.application.dto.CartSummaryResponse;
import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import com.sahaja.swalayan.ecommerce.application.dto.AddCartItemRequest;
import com.sahaja.swalayan.ecommerce.application.dto.UpdateCartItemRequest;
import java.util.UUID;

public interface CartService {
    Cart getCartForUser(UUID userId);
    Cart addItemToCart(UUID userId, AddCartItemRequest request);
    Cart updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request);
    Cart removeCartItem(UUID userId, UUID itemId);
    Cart clearCart(UUID userId);
    CartSummaryResponse getCartSummary(UUID id);
}
