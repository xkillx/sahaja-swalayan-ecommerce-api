package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.domain.service.CartService;
import com.sahaja.swalayan.ecommerce.application.dto.AddCartItemRequest;
import com.sahaja.swalayan.ecommerce.application.dto.UpdateCartItemRequest;
import com.sahaja.swalayan.ecommerce.application.dto.CartResponse;
import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import jakarta.validation.Valid;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.getCartForUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", CartResponse.fromEntity(cart)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addCartItem(@Valid @RequestBody AddCartItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", CartResponse.fromEntity(cart)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", CartResponse.fromEntity(cart)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> deleteCartItem(@PathVariable UUID itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed", CartResponse.fromEntity(cart)));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", CartResponse.fromEntity(cart)));
    }
}
