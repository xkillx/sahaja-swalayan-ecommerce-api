package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.application.dto.CartItemSummaryDTO;
import com.sahaja.swalayan.ecommerce.application.dto.CartSummaryResponse;
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
import com.sahaja.swalayan.ecommerce.common.CartItemNotFoundException;
import com.sahaja.swalayan.ecommerce.common.InsufficientQuantityException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
        if (product.getQuantity() < request.getQuantity()) {
            log.warn("Insufficient quantity for productId={}, requested={}, available={}", product.getId(), request.getQuantity(), product.getQuantity());
            throw new InsufficientQuantityException("Insufficient quantity: only " + product.getQuantity() + " left");
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
            if (product.getQuantity() < newQty) {
                log.warn("Insufficient quantity for productId={} when updating cart item, requested total={}, available={}", product.getId(), newQty, product.getQuantity());
                throw new InsufficientQuantityException("Insufficient quantity: only " + product.getQuantity() + " left");
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
        if (product.getQuantity() < request.getQuantity()) {
            log.warn("Insufficient quantity for productId={}, requested={}, available={}", product.getId(), request.getQuantity(), product.getQuantity());
            throw new InsufficientQuantityException("Insufficient quantity: only " + product.getQuantity() + " left");
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

    @Transactional
    public CartSummaryResponse getCartSummary(UUID userId) {
        Cart cart = cartRepository.findByUserIdWithItemsAndProduct(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        List<CartItemSummaryDTO> items = cart.getItems().stream()
                .map(item -> {
                    var product = item.getProduct();
                    if (product == null) throw new ProductNotFoundException("CartItem has no product");

                    BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                    BigDecimal qty   = BigDecimal.valueOf(item.getQuantity());
                    BigDecimal subtotal = price.multiply(qty);

                    return new CartItemSummaryDTO(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            price,
                            item.getQuantity(),
                            subtotal,
                            product.getImageUrl(),
                            product.getWeight()
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemSummaryDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartSummaryResponse(cart.getId(), items, total);
    }

}
