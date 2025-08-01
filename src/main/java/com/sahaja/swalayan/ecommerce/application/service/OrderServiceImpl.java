package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.PaymentMethod;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.domain.model.cart.Cart;
import com.sahaja.swalayan.ecommerce.domain.model.cart.CartItem;
import com.sahaja.swalayan.ecommerce.domain.model.order.OrderItem;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.OrderItemRepository;
import com.sahaja.swalayan.ecommerce.domain.service.InventoryService;
import com.sahaja.swalayan.ecommerce.domain.service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.sahaja.swalayan.ecommerce.common.CartEmptyException;
import com.sahaja.swalayan.ecommerce.common.ForbiddenException;
import com.sahaja.swalayan.ecommerce.common.OrderNotFoundException;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartServiceImpl cartService;
    private final InventoryService inventoryService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            CartServiceImpl cartService, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        log.debug("OrderServiceImpl initialized");
    }

    @Override
    @Transactional
    public Order createOrderFromCart(UUID userId, String shippingAddress, PaymentMethod paymentMethod) {
        log.debug("Creating order for userId={}, shippingAddress={}, paymentMethod={}", userId, shippingAddress,
                paymentMethod);

        // 1. Fetch the cart items for the user
        Cart cart = cartService.getCartForUser(userId);
        log.debug("Fetched cart for userId={}: cartId={}, itemCount={}", userId, cart.getId(),
                cart.getItems() != null ? cart.getItems().size() : 0);
        Set<CartItem> cartItems = cart.getItems();
        log.debug("Cart items: {}", cartItems);
        if (cartItems == null || cartItems.isEmpty()) {
            log.warn("Cart is empty for userId={}", userId);
            throw new CartEmptyException("Cart is empty");
        }

        // 2. Calculate the total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Calculated total amount for order: {}", totalAmount);

        // 3. Reserve stock by calling InventoryService
        log.debug("Reserving stock for cart items on InventoryService");
        inventoryService.reserveStock(cartItems);
        log.debug("Stock reserved successfully for userId={}", userId);

        // 4. Save the order
        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .status(Status.PENDING)
                .shippingAddress(shippingAddress)
                .paymentMethod(paymentMethod)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);
        log.debug("Order saved: orderId={}, userId={}, totalAmount={}", order.getId(), userId, totalAmount);

        // 5. Save order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.getProduct().getId())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getProduct().getPrice())
                    .build();
            orderItemRepository.save(orderItem);
            log.debug("Order item saved: orderId={}, productId={}, quantity={}, pricePerUnit={}", order.getId(),
                    cartItem.getProduct().getId(), cartItem.getQuantity(), cartItem.getProduct().getPrice());
        }

        // 6. Clear the user's cart
        cartService.clearCart(userId);
        log.debug("Cleared cart for userId={}", userId);

        // 7. Assign order items to order
        List<OrderItem> savedOrderItems = orderItemRepository.findByOrderId(order.getId());
        order.setItems(savedOrderItems);

        // 8. Return the created order
        log.debug("Order creation complete for userId={}, orderId={}", userId, order.getId());
        return order;
    }

    @Override
    public List<Order> getOrdersByUser(UUID userId) {
        log.debug("Fetching orders for userId={}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        log.debug("Fetched {} orders for userId={}", orders.size(), userId);
        return orders;
    }

    @Override
    public Order getOrderByIdForUser(UUID orderId, UUID userId) {
        log.debug("Fetching order by id={} for userId={}", orderId, userId);
        var orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.debug("Order not found: {} for userId={}", orderId, userId);
            throw new OrderNotFoundException("Order not found: " + orderId);
        }
        var order = orderOpt.get();
        if (!userId.equals(order.getUserId())) {
            log.debug("Forbidden access: userId={} tried to access orderId={}", userId, orderId);
            throw new ForbiddenException("Access denied to order: " + orderId);
        }
        log.debug("Fetched orderId={} for userId={}", orderId, userId);
        return order;
    }

    /**
     * Update the status of an order and log the change.
     * Example: after payment webhook, shipment, etc.
     */
    @Transactional
    public Order updateOrderStatus(UUID orderId, Status newStatus, String note, UUID userId) {
        log.debug("Updating status for orderId={} by userId={} to status={} (note={})", orderId, userId, newStatus, note);
        var order = getOrderByIdForUser(orderId, userId);
        Status oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.debug("Order status updated: orderId={} userId={} {} -> {} (note={})", orderId, userId, oldStatus, newStatus, note);
        // If status history tracking is needed, insert here
        return order;
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId, UUID userId) {
        // Only allow cancel if status is PENDING
        log.debug("Attempting to cancel orderId={} for userId={} (PENDING only)", orderId, userId);
        var order = getOrderByIdForUser(orderId, userId);
        if (order.getStatus() != Status.PENDING) {
            log.warn("Cannot cancel orderId={} for userId={}: status is {} (must be PENDING)", orderId, userId, order.getStatus());
            throw new ForbiddenException("Order cannot be cancelled unless status is PENDING");
        }
        order.setStatus(Status.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.debug("Order cancelled (PENDING): orderId={} for userId={}", orderId, userId);
        // If status history tracking is needed, insert here
        return order;
    }
}
