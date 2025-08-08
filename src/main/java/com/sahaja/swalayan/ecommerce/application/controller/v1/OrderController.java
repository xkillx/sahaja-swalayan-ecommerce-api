package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.OrderDTO;
import com.sahaja.swalayan.ecommerce.application.dto.OrderRequest;
import com.sahaja.swalayan.ecommerce.application.mapper.OrderMapper;
import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.service.OrderService;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiCreateOrderOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiGetOrderByIdOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiGetUserOrdersOperation;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiCancelOrderOperation;
import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @ApiCreateOrderOperation
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        UUID userId = userDetails.getId();
        Order order = orderService.createOrderFromCart(userId, request.getAddressId());
        return ResponseEntity.ok(ApiResponse.success("Order created", toOrderDTO(order)));
    }

    @ApiGetUserOrdersOperation
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        List<Order> orders = orderService.getOrdersByUser(userId);
        List<OrderDTO> dtos = orders.stream().map(this::toOrderDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", dtos));
    }

    @ApiGetOrderByIdOperation
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = userDetails.getId();
        Order order = orderService.getOrderByIdForUser(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order fetched", toOrderDTO(order)));
    }

    @ApiCancelOrderOperation
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = userDetails.getId();
        Order order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", toOrderDTO(order)));
    }

    private OrderDTO toOrderDTO(Order order) {
        return orderMapper.toOrderDTO(order);
    }
}
