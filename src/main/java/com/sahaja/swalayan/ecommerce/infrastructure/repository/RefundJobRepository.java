package com.sahaja.swalayan.ecommerce.infrastructure.repository;

import com.sahaja.swalayan.ecommerce.domain.model.order.RefundJob;
import com.sahaja.swalayan.ecommerce.domain.model.order.RefundJob.RefundJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefundJobRepository extends JpaRepository<RefundJob, UUID> {
    @Query("select j from RefundJob j where j.status = :status and (j.nextRunAt is null or j.nextRunAt <= :now) order by j.createdAt asc")
    List<RefundJob> findDue(@Param("now") LocalDateTime now, @Param("status") RefundJobStatus status);

    List<RefundJob> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
