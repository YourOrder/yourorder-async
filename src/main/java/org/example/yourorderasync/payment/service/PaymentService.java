package org.example.yourorderasync.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.kafka.event.OrderCreatedEvent;
import org.example.yourorderasync.payment.entity.PaymentEntity;
import org.example.yourorderasync.payment.repository.PaymentRepository;
import org.example.yourorderasync.payment.status.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentEntity processPayment(OrderCreatedEvent event) {
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .amount(event.totalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created for orderId={}", event.orderId());

        boolean success = simulatePayment();

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProcessedAt(LocalDateTime.now());
            log.info("Payment COMPLETED for orderId={}", event.orderId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProcessedAt(LocalDateTime.now());
            log.info("Payment FAILED for orderId={}", event.orderId());
        }

        return paymentRepository.save(payment);
    }

    public PaymentEntity getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));
    }

    private boolean simulatePayment() {
        return Math.random() > 0.2;
    }
}
