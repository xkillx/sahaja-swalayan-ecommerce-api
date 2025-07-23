package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartRepository;
import com.sahaja.swalayan.ecommerce.domain.service.CartService;
import com.sahaja.swalayan.ecommerce.domain.repository.cart.CartItemRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.ProductRepository;
import com.sahaja.swalayan.ecommerce.application.dto.AddCartItemRequest;
import com.sahaja.swalayan.ecommerce.application.dto.UpdateCartItemRequest;
import com.sahaja.swalayan.ecommerce.common.CartNotFoundException;
import com.sahaja.swalayan.ecommerce.common.ProductNotFoundException;
import com.sahaja.swalayan.ecommerce.common.InsufficientStockException;
import com.sahaja.swalayan.ecommerce.common.CartItemNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final com.sahaja.swalayan.ecommerce.domain.repository.UserRepository userRepository; // Inject user repo

    public CartServiceImpl(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            com.sahaja.swalayan.ecommerce.domain.repository.UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        log.debug("CartServiceImpl initialized");
    }

    @Override
    public Cart getCartForUser(UUID userId) {
        log.debug("getCartForUser called with userId={}", userId);
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
    }

    @Override
    public Cart addItemToCart(UUID userId, AddCartItemRequest request) {
        log.debug("addItemToCart called with userId={}, productId={}, quantity={}", userId, request.getProductId(), request.getQuantity());
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.debug("No cart found for userId={}, creating new cart", userId);
                    var user = userRepository.findById(userId)
                        .orElseThrow(() -> new CartNotFoundException("User not found for cart creation"));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (product.getStock() == null) {
            log.error("Product stock is null for productId={}", product.getId());
            throw new ProductNotFoundException("Product stock is missing");
        }
        if (product.getStock().getValue() < request.getQuantity()) {
            log.warn("Insufficient stock for productId={}, requested={}, available={}", product.getId(), request.getQuantity(), product.getStock().getValue());
            throw new InsufficientStockException("Insufficient stock for product");
        }
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> {
                    if (item.getProduct() == null) {
                        log.error("CartItem with id={} has no product", item.getId());
                        throw new ProductNotFoundException("CartItem has no product");
                    }
                    return item.getProduct().getId().equals(product.getId());
                })
                .findFirst();
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (product.getStock().getValue() < newQty) {
                log.warn("Insufficient stock for productId={} when updating cart item, requested total={}, available={}", product.getId(), newQty, product.getStock().getValue());
                throw new InsufficientStockException("Insufficient stock for product");
            }
            log.debug("Updating quantity for existing cart item id={} to {}", item.getId(), newQty);
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            log.debug("Adding new cart item for productId={} to cartId={}", product.getId(), cart.getId());
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        log.debug("updateCartItem called with userId={}, itemId={}, quantity={}", userId, itemId, request.getQuantity());
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));
        Product product = item.getProduct();
        if (product == null) {
            log.error("CartItem with id={} has no product", item.getId());
            throw new ProductNotFoundException("CartItem has no product");
        }
        if (product.getStock() == null) {
            log.error("Product stock is null for productId={}", product.getId());
            throw new ProductNotFoundException("Product stock is missing");
        }
        if (product.getStock().getValue() < request.getQuantity()) {
            log.warn("Insufficient stock for productId={}, requested={}, available={}", product.getId(), request.getQuantity(), product.getStock().getValue());
            throw new InsufficientStockException("Insufficient stock for product");
        }
        log.debug("Updating cart item id={} quantity to {}", item.getId(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeCartItem(UUID userId, UUID itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));
        cart.getItems().remove(item);
        cartItemRepository.deleteById(itemId);
        return cartRepository.save(cart);
    }

    @Override
    public Cart clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        cart.getItems().forEach(item -> cartItemRepository.deleteById(item.getId()));
        cart.getItems().clear();
        return cartRepository.save(cart);
    }
}
