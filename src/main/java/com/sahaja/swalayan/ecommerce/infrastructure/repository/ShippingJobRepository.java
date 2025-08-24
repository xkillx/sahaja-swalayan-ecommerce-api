package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob;
import com.sahaja.swalayan.ecommerce.domain.model.order.ShippingJob.ShippingJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShippingJobRepository extends JpaRepository<ShippingJob, UUID> {
    @Query("select j from ShippingJob j where j.status = :status and (j.nextRunAt is null or j.nextRunAt <= :now) order by j.createdAt asc")
    List<ShippingJob> findDue(@Param("now") LocalDateTime now, @Param("status") ShippingJobStatus status);

    List<ShippingJob> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
}