package com.sahaja.swalayan.ecommerce.domain.repository.cart;

import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Optional<Cart> findById(UUID id);
    Optional<Cart> findByUserId(UUID userId);
    Cart save(Cart cart);
    void deleteById(UUID id);
    void delete(Cart cart);
    Optional<Cart> findByUserIdWithItemsAndProduct(UUID userId);

}
