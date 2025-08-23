package com.sahaja.swalayan.ecommerce.application.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDTO {
    private UUID id;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("order_date")
    private LocalDateTime orderDate;
    @JsonProperty("items_total")
    private BigDecimal itemsTotal;
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    private String status;
    @JsonProperty("address_id")
    private UUID addressId;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;

    // Shipping details
    @JsonProperty("shipping_courier_code")
    private String shippingCourierCode; // e.g., "jne", "pos", "sicepat"
    @JsonProperty("shipping_courier_service")
    private String shippingCourierService; // e.g., "REG", "YES"
    @JsonProperty("shipping_courier_service_name")
    private String shippingCourierServiceName; // e.g., "JNE Regular"
    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost;
    @JsonProperty("shipping_order_id")
    private String shippingOrderId; // Biteship's shipment/order ID
    @JsonProperty("tracking_id")
    private String trackingId; // Courier tracking number
    @JsonProperty("estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
    @JsonProperty("shipping_status")
    private String shippingStatus; // e.g., "pending", "on_delivery", "delivered"

    // Enriched courier/driver info
    @JsonProperty("courier_waybill_id")
    private String courierWaybillId;
    @JsonProperty("courier_company")
    private String courierCompany;
    @JsonProperty("courier_type")
    private String courierType;
    @JsonProperty("courier_driver_name")
    private String courierDriverName;
    @JsonProperty("courier_driver_phone")
    private String courierDriverPhone;
    @JsonProperty("courier_driver_plate_number")
    private String courierDriverPlateNumber;
    @JsonProperty("courier_driver_photo_url")
    private String courierDriverPhotoUrl;
    @JsonProperty("courier_link")
    private String courierLink;
    @JsonProperty("shipping_updated_at")
    private LocalDateTime shippingUpdatedAt;
}
