package org.example.yourorderasync.payment.repository;

import org.example.yourorderasync.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByOrderId(UUID orderId);

    List<PaymentEntity> findByStatusAndCreatedAtBefore(org.example.yourorderasync.payment.status.PaymentStatus status, LocalDateTime createdAt);
}
