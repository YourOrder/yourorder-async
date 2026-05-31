package org.example.yourorderasync.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.kafka.event.OrderCreatedEvent;
import org.example.yourorderasync.kafka.event.StockReservedEvent;
import org.example.yourorderasync.kafka.producer.AsyncEventProducer;
import org.example.yourorderasync.notification.entity.NotificationEntity;
import org.example.yourorderasync.notification.service.NotificationService;
import org.example.yourorderasync.payment.entity.PaymentEntity;
import org.example.yourorderasync.payment.service.PaymentService;
import org.example.yourorderasync.payment.status.PaymentStatus;
import org.example.yourorderasync.report.entity.SalesReportEntity;
import org.example.yourorderasync.report.service.SalesReportService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final SalesReportService salesReportService;
    private final AsyncEventProducer asyncEventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onOrderCreated(String message) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
        log.info("Received order.created: orderId={}", event.orderId());

        PaymentEntity payment = paymentService.processPayment(event);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            asyncEventProducer.sendPaymentCompleted(event.orderId());

            if (event.companyId() != null) {
                SalesReportEntity report = salesReportService.updateReport(event, LocalDate.now());
                asyncEventProducer.sendReportGenerated(report);
            }

            NotificationEntity notification = notificationService.sendNotification(
                    event.userId(),
                    event.orderId(),
                    "Ваш заказ оплачен успешно. Сумма: " + event.totalAmount()
            );
            asyncEventProducer.sendNotificationSent(
                    notification.getOrderId(),
                    notification.getUserId(),
                    notification.getMessage()
            );
        } else {
            asyncEventProducer.sendPaymentFailed(event.orderId());

            NotificationEntity notification = notificationService.sendNotification(
                    event.userId(),
                    event.orderId(),
                    "Оплата заказа не прошла. Попробуйте снова."
            );
            asyncEventProducer.sendNotificationSent(
                    notification.getOrderId(),
                    notification.getUserId(),
                    notification.getMessage()
            );
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.stock-reserved}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onStockReserved(String message) throws Exception {
        StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);
        log.info("Received stock.reserved: orderId={}", event.orderId());
    }
}