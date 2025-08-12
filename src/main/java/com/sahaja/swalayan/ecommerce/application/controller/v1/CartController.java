package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.*;
import com.sahaja.swalayan.ecommerce.domain.service.CartService;
import jakarta.validation.Valid;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiCrudResponses;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @Operation(summary = "Get user's cart", description = "Retrieves the current authenticated user's shopping cart, including all cart items and totals.")
    @ApiCrudResponses
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.getCartForUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", CartResponse.fromEntity(cart)));
    }

    @Operation(summary = "Add item to cart", description = "Adds a new item to the authenticated user's cart. If the item already exists, its quantity will be increased.")
    @ApiCrudResponses
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addCartItem(@Valid @RequestBody AddCartItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", CartResponse.fromEntity(cart)));
    }

    @Operation(summary = "Update cart item", description = "Updates the quantity or details of a specific item in the authenticated user's cart by item ID.")
    @ApiCrudResponses
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", CartResponse.fromEntity(cart)));
    }

    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the authenticated user's cart by item ID.")
    @ApiCrudResponses
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> deleteCartItem(@PathVariable UUID itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed", CartResponse.fromEntity(cart)));
    }

    @Operation(summary = "Clear cart", description = "Removes all items from the authenticated user's cart, leaving it empty.")
    @ApiCrudResponses
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        var cart = cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", CartResponse.fromEntity(cart)));
    }

    @Operation(
            summary = "Get cart summary",
            description = "Returns the authenticated user's cart with item prices, subtotals, and total."
    )
    @ApiCrudResponses
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID userId = userDetails.getId();
        CartSummaryResponse summary = cartService.getCartSummary(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", summary));
    }


}
