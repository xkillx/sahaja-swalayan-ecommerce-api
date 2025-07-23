package com.sahaja.swalayan.ecommerce.infrastructure.repository.cart;

import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItem, UUID> {
}
