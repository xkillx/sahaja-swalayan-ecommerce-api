package com.sahaja.swalayan.ecommerce.application.service;

import com.sahaja.swalayan.ecommerce.domain.model.order.Order;
import com.sahaja.swalayan.ecommerce.domain.model.order.OrderItem;
import com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob;
import com.sahaja.swalayan.ecommerce.domain.model.order.Status;
import com.sahaja.swalayan.ecommerce.domain.model.product.Product;
import com.sahaja.swalayan.ecommerce.domain.repository.*;
import com.sahaja.swalayan.ecommerce.infrastructure.config.ShippingOriginProperties;
import com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto.*;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.ShippingJobRepository;
import com.sahaja.swalayan.ecommerce.infrastructure.repository.StoreSettingsRepository;
import com.sahaja.swalayan.ecommerce.domain.model.settings.StoreSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShippingJobWorker {

    private final ShippingJobRepository shippingJobRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShippingServiceImpl shippingService; // reuse service for API call
    private final ShippingOriginProperties shippingOriginProperties;
    private final StoreSettingsRepository storeSettingsRepository;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void process() {
        var due = shippingJobRepository.findDue(LocalDateTime.now(), ShippingJob.ShippingJobStatus.PENDING);
        for (var job : due) {
            try {
                job.setStatus(ShippingJob.ShippingJobStatus.IN_PROGRESS);
                job.setLastError(null);
                shippingJobRepository.save(job);

                if (job.getType() == ShippingJob.ShippingJobType.CREATE_ORDER) {
                    handleCreate(job);
                } else {
                    // Unknown type; mark failed
                    job.setStatus(ShippingJob.ShippingJobStatus.FAILED);
                    job.setLastError("Unsupported job type");
                    shippingJobRepository.save(job);
                }
            } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
                int status = httpEx.getStatusCode().value();
                boolean retryable = status >= 500 || status == 429;
                int attempts = job.getAttempts() + 1;
                job.setAttempts(attempts);
                job.setLastError("HTTP " + status + ": " + httpEx.getResponseBodyAsString());
                if (retryable && attempts < 7) {
                    job.setStatus(ShippingJob.ShippingJobStatus.PENDING);
                    job.setNextRunAt(nextBackoff(attempts));
                } else {
                    job.setStatus(ShippingJob.ShippingJobStatus.FAILED);
                }
                shippingJobRepository.save(job);
            } catch (Exception ex) {
                boolean retryable = !(ex instanceof IllegalArgumentException || ex instanceof IllegalStateException);
                int attempts = job.getAttempts() + 1;
                job.setAttempts(attempts);
                job.setLastError(ex.getMessage());
                if (retryable && attempts < 7) {
                    job.setStatus(ShippingJob.ShippingJobStatus.PENDING);
                    job.setNextRunAt(nextBackoff(attempts));
                } else {
                    job.setStatus(ShippingJob.ShippingJobStatus.FAILED);
                }
                shippingJobRepository.save(job);
            }
        }
    }

    private void handleCreate(ShippingJob job) {
        Order order = orderRepository.findById(job.getOrderId()).orElse(null);
        if (order == null) throw new IllegalStateException("Order not found");

        // Idempotency: skip if shipping already created
        if (order.getShippingOrderId() != null && !order.getShippingOrderId().isBlank()) {
            job.setStatus(ShippingJob.ShippingJobStatus.SUCCEEDED);
            shippingJobRepository.save(job);
            return;
        }
        if (order.getShippingCourierCode() == null || order.getShippingCourierService() == null) {
            throw new IllegalStateException("Missing courier selection on order");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems == null || orderItems.isEmpty()) throw new IllegalStateException("No order items");
        var itemDTOs = new ArrayList<OrderItemDTO>();
        for (OrderItem oi : orderItems) {
            Product product = productRepository.findById(oi.getProductId()).orElse(null);
            if (product == null) continue;
            Integer value = oi.getPricePerUnit() != null ? oi.getPricePerUnit().setScale(0, RoundingMode.HALF_UP).intValue() : null;
            var item = OrderItemDTO.builder()
                    .name(product.getName())
                    .description(product.getDescription())
                    .sku(product.getSku())
                    .value(value)
                    .quantity(oi.getQuantity())
                    .weight(product.getWeight())
                    .height(product.getHeight())
                    .length(product.getLength())
                    .width(product.getWidth())
                    .build();
            itemDTOs.add(item);
        }
        if (itemDTOs.isEmpty()) throw new IllegalStateException("No valid items to ship");

        var addr = order.getShippingAddress();
        if (addr == null) throw new IllegalStateException("Missing shipping address");
        CoordinateDTO destinationCoordinate = null;
        if (addr.getLatitude() != null && addr.getLongitude() != null) {
            destinationCoordinate = CoordinateDTO.builder().latitude(addr.getLatitude()).longitude(addr.getLongitude()).build();
        }
        // Origin from StoreSettings
        CoordinateDTO originCoordinate = null;
        String originAddress = null;
        String shipperOrganization = null;
        var storeSettingsOpt = storeSettingsRepository.findAll().stream().findFirst();
        if (storeSettingsOpt.isPresent()) {
            StoreSettings s = storeSettingsOpt.get();
            if (s.getLatitude() != null && s.getLongitude() != null) {
                originCoordinate = CoordinateDTO.builder().latitude(s.getLatitude()).longitude(s.getLongitude()).build();
            }
            originAddress = s.getAddressLine();
            shipperOrganization = s.getStoreName();
        }
        if (destinationCoordinate == null) throw new IllegalStateException("Destination coordinate missing");
        if (originCoordinate == null) throw new IllegalStateException("Origin coordinate missing");

        var destinationEmail = userRepository.findById(order.getUserId()).map(u -> u.getEmail()).orElse(null);

        var builder = CreateOrderRequestDTO.builder()
                .referenceId(order.getId().toString())
                .shipperContactName(shippingOriginProperties.getContactName())
                .shipperContactPhone(shippingOriginProperties.getContactPhone())
                .shipperContactEmail(shippingOriginProperties.getContactEmail())
                .shipperOrganization(shipperOrganization != null ? shipperOrganization : shippingOriginProperties.getOrganization())
                .originContactName(shippingOriginProperties.getContactName())
                .originContactPhone(shippingOriginProperties.getContactPhone())
                .originContactEmail(shippingOriginProperties.getContactEmail())
                .originAddress(originAddress)
                .originNote(null)
                .originPostalCode(null)
                .originAreaId(null)
                .originLocationId(null)
                .originCollectionMethod(null)
                .originCoordinate(originCoordinate)
                .destinationContactName(addr.getContactName())
                .destinationContactPhone(addr.getContactPhone())
                .destinationContactEmail(destinationEmail)
                .destinationAddress(addr.getAddressLine())
                .destinationPostalCode(addr.getPostalCode())
                .destinationAreaId(addr.getAreaId())
                .destinationLocationId(null)
                .destinationProofOfDelivery(null)
                .destinationProofOfDeliveryNote(null)
                .destinationCashOnDelivery(null)
                .destinationCashOnDeliveryType(null)
                .destinationCoordinate(destinationCoordinate)
                .courierCompany(order.getShippingCourierCode())
                .courierType(order.getShippingCourierService() != null ? order.getShippingCourierService().toLowerCase() : null)
                .courierInsurance(null)
                .deliveryType("now");
        for (var it : itemDTOs) builder.item(it);
        var req = builder.build();

        // Call shipping service (idempotency header is managed inside client; if not, we rely on referenceId semantics)
        var response = shippingService.createOrder(req);
        if (response != null && response.isSuccess()) {
            String shippingOrderId = response.getId();
            String trackingId = response.getCourier() != null ? response.getCourier().getTrackingId() : null;
            String shippingStatus = response.getStatus();

            order.setShippingOrderId(shippingOrderId);
            order.setTrackingId(trackingId);
            order.setShippingStatus(shippingStatus != null ? shippingStatus : "waiting_pickup");
            if (order.getStatus() == Status.PENDING) order.setStatus(Status.CONFIRMED);
            orderRepository.save(order);

            job.setStatus(ShippingJob.ShippingJobStatus.SUCCEEDED);
            shippingJobRepository.save(job);
        } else {
            throw new IllegalStateException("Create shipping order failed");
        }
    }

    private LocalDateTime nextBackoff(int attempts) {
        switch (attempts) {
            case 1: return LocalDateTime.now().plusMinutes(1);
            case 2: return LocalDateTime.now().plusMinutes(5);
            case 3: return LocalDateTime.now().plusMinutes(15);
            case 4: return LocalDateTime.now().plusHours(1);
            case 5: return LocalDateTime.now().plusHours(6);
            case 6: return LocalDateTime.now().plusHours(12);
            default: return LocalDateTime.now().plusDays(1);
        }
    }
}