package com.sahaja.swalayan.ecommerce.infrastructure.repository.cart;

import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartItemRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CartItemRepositoryImpl implements CartItemRepository {
    private final CartItemJpaRepository cartItemJpaRepository;

    public CartItemRepositoryImpl(CartItemJpaRepository cartItemJpaRepository) {
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Override
    public Optional<CartItem> findById(UUID id) {
        return cartItemJpaRepository.findById(id);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemJpaRepository.save(cartItem);
    }

    @Override
    public void deleteById(UUID id) {
        cartItemJpaRepository.deleteById(id);
    }
}
