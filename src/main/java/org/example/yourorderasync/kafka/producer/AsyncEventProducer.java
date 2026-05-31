package org.example.yourorderasync.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.kafka.event.NotificationSentEvent;
import org.example.yourorderasync.kafka.event.PaymentCompletedEvent;
import org.example.yourorderasync.kafka.event.PaymentFailedEvent;
import org.example.yourorderasync.kafka.event.ReportGeneratedEvent;
import org.example.yourorderasync.report.entity.SalesReportEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncEventProducer {

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    private static final String NOTIFICATION_SENT_TOPIC = "notification.sent";
    private static final String REPORT_GENERATED_TOPIC = "report.generated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCompleted(UUID orderId) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(orderId);
        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, orderId.toString(), event);
        log.info("Sent payment.completed for orderId={}", orderId);
    }

    public void sendPaymentFailed(UUID orderId) {
        PaymentFailedEvent event = new PaymentFailedEvent(orderId);
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, orderId.toString(), event);
        log.info("Sent payment.failed for orderId={}", orderId);
    }

    public void sendNotificationSent(UUID orderId, UUID userId, String message) {
        NotificationSentEvent event = new NotificationSentEvent(
                orderId, userId, message, LocalDateTime.now()
        );
        kafkaTemplate.send(NOTIFICATION_SENT_TOPIC, orderId.toString(), event);
        log.info("Sent notification.sent for orderId={}", orderId);
    }

    public void sendReportGenerated(SalesReportEntity report) {
        ReportGeneratedEvent event = new ReportGeneratedEvent(
                report.getId(),
                report.getCompanyId(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getTotalRevenue(),
                report.getOrderCount()
        );
        kafkaTemplate.send(REPORT_GENERATED_TOPIC, report.getId().toString(), event);
        log.info("Sent report.generated for companyId={}", report.getCompanyId());
    }
}
