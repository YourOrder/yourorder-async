package org.example.yourorderasync.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.kafka.event.OrderCreatedEvent;
import org.example.yourorderasync.kafka.producer.AsyncEventProducer;
import org.example.yourorderasync.notification.entity.NotificationEntity;
import org.example.yourorderasync.notification.service.NotificationService;
import org.example.yourorderasync.payment.entity.PaymentEntity;
import org.example.yourorderasync.payment.repository.PaymentRepository;
import org.example.yourorderasync.payment.status.PaymentStatus;
import org.example.yourorderasync.report.entity.SalesReportEntity;
import org.example.yourorderasync.report.service.SalesReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AsyncEventProducer asyncEventProducer;
    private final NotificationService notificationService;
    private final SalesReportService salesReportService;

    public PaymentEntity createPendingPayment(OrderCreatedEvent event) {
        return paymentRepository.findByOrderId(event.orderId())
                .orElseGet(() -> savePendingPayment(event));
    }

    public PaymentEntity confirmPayment(UUID paymentId) {
        PaymentEntity payment = getById(paymentId);
        ensurePending(payment);

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        PaymentEntity saved = paymentRepository.save(payment);

        asyncEventProducer.sendPaymentCompleted(saved.getOrderId());

        if (saved.getCompanyId() != null) {
            SalesReportEntity report = salesReportService.updateReport(saved.getCompanyId(), saved.getAmount(), LocalDate.now());
            asyncEventProducer.sendReportGenerated(report);
        }

        NotificationEntity notification = notificationService.sendNotification(
                saved.getUserId(),
                saved.getOrderId(),
                "Оплата заказа подтверждена администратором. Сумма: " + saved.getAmount()
        );
        asyncEventProducer.sendNotificationSent(
                notification.getOrderId(),
                notification.getUserId(),
                notification.getMessage()
        );

        log.info("Payment CONFIRMED for orderId={}", saved.getOrderId());
        return saved;
    }

    public PaymentEntity cancelPayment(UUID paymentId) {
        PaymentEntity payment = getById(paymentId);
        ensurePending(payment);
        return failPayment(payment, PaymentStatus.CANCELLED, "Оплата заказа отменена администратором.");
    }

    @Transactional(readOnly = true)
    public List<PaymentEntity> getPayments(PaymentStatus status) {
        if (status == null) {
            return paymentRepository.findAll();
        }
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getStatus() == status)
                .toList();
    }

    @Scheduled(fixedDelay = 30_000)
    public void expirePendingPayments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold)
                .forEach(payment -> failPayment(payment, PaymentStatus.FAILED, "Оплата не подтверждена за 5 минут. Заказ отменен."));
    }

    public PaymentEntity getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));
    }

    private PaymentEntity savePendingPayment(OrderCreatedEvent event) {
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .companyId(event.companyId())
                .amount(event.totalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Pending payment created for orderId={}", event.orderId());
        return payment;
    }

    private PaymentEntity failPayment(PaymentEntity payment, PaymentStatus status, String message) {
        payment.setStatus(status);
        payment.setProcessedAt(LocalDateTime.now());
        PaymentEntity saved = paymentRepository.save(payment);

        asyncEventProducer.sendPaymentFailed(saved.getOrderId());

        NotificationEntity notification = notificationService.sendNotification(
                saved.getUserId(),
                saved.getOrderId(),
                message
        );
        asyncEventProducer.sendNotificationSent(
                notification.getOrderId(),
                notification.getUserId(),
                notification.getMessage()
        );

        log.info("Payment {} for orderId={}", status, saved.getOrderId());
        return saved;
    }

    private PaymentEntity getById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }

    private void ensurePending(PaymentEntity payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payment can be changed");
        }
    }
}
