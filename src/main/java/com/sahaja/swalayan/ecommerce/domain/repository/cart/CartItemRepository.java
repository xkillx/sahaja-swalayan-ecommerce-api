package com.sahaja.swalayan.ecommerce.domain.repository.cart;

import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {
    Optional<CartItem> findById(UUID id);
    CartItem save(CartItem cartItem);
    void deleteById(UUID id);
}
