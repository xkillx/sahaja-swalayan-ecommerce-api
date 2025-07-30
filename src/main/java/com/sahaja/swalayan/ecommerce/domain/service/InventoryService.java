package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import java.util.Set;

public interface InventoryService {
    void reserveStock(Set<CartItem> cartItems);
}
