package com.sahaja.swalayan.ecommerce.infrastructure.repository.cart;

import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(UUID userId);

    @Query("""
               select distinct c
               from Cart c
               left join fetch c.items i
               left join fetch i.product p
               where c.user.id = :userId
            """)
    Optional<Cart> findByUserIdWithItemsAndProduct(UUID userId);
}
