package com.sahaja.swalayan.ecommerce.infrastructure.repository.cart;

import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CartRepositoryImpl implements CartRepository {
    private final CartJpaRepository cartJpaRepository;

    public CartRepositoryImpl(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }

    @Override
    public Optional<Cart> findById(UUID id) {
        return cartJpaRepository.findById(id);
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return cartJpaRepository.findByUserId(userId);
    }

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }

    @Override
    public void deleteById(UUID id) {
        cartJpaRepository.deleteById(id);
    }

    @Override
    public void delete(Cart cart) {
        cartJpaRepository.delete(cart);
    }
}
