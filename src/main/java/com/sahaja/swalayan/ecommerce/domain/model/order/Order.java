package com.sahaja.swalayan.ecommerce.domain.model.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.sahaja.swalayan.ecommerce.domain.model.user.Address;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "items_total", precision = 18, scale = 2, nullable = false)
    private BigDecimal itemsTotal; // only sum of items (excludes shipping)

    @Column(nullable = false)
    private BigDecimal totalAmount; // final = itemsTotal + shippingCost

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;

    @Column(name = "shipping_courier_code", length = 50)
    private String shippingCourierCode; // e.g., "jne", "pos", "sicepat"

    @Column(name = "shipping_courier_service", length = 100)
    private String shippingCourierService; // e.g., "REG", "YES"

    @Column(name = "shipping_courier_service_name", length = 100)
    private String shippingCourierServiceName; // e.g., "JNE Regular"

    @Column(name = "shipping_cost", precision = 18, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "shipping_order_id", length = 100)
    private String shippingOrderId; // Biteship's shipment/order ID

    @Column(name = "tracking_id", length = 100)
    private String trackingId; // Courier tracking number

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "shipping_status", length = 50)
    private String shippingStatus; // e.g., "pending", "on_delivery", "delivered"

    // --- Enriched shipping/courier details (populated via webhook) ---
    @Column(name = "courier_waybill_id", length = 100)
    private String courierWaybillId;

    @Column(name = "courier_company", length = 50)
    private String courierCompany; // e.g., gojek, jne

    @Column(name = "courier_type", length = 50)
    private String courierType; // e.g., instant, same_day, reg

    @Column(name = "courier_driver_name", length = 150)
    private String courierDriverName;

    @Column(name = "courier_driver_phone", length = 50)
    private String courierDriverPhone;

    @Column(name = "courier_driver_plate_number", length = 50)
    private String courierDriverPlateNumber;

    @Column(name = "courier_driver_photo_url", length = 500)
    private String courierDriverPhotoUrl;

    @Column(name = "courier_link", length = 500)
    private String courierLink; // provider tracking link

    @Column(name = "shipping_updated_at")
    private LocalDateTime shippingUpdatedAt; // last provider update time

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items;
    
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
